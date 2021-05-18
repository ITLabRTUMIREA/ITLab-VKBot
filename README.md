# Notify analyzer for IT Lab system

Status | master | develop
------ | ------ | -------
Build  | [![Build Status](https://dev.azure.com/rtuitlab/RTU%20IT%20Lab/_apis/build/status/ITLab/ITLab-Notify?branchName=master)](https://dev.azure.com/rtuitlab/RTU%20IT%20Lab/_build/latest?definitionId=165&branchName=master) | [![Build Status](https://dev.azure.com/rtuitlab/RTU%20IT%20Lab/_apis/build/status/ITLab/ITLab-Notify?branchName=develop)](https://dev.azure.com/rtuitlab/RTU%20IT%20Lab/_build/latest?definitionId=165&branchName=develop) |

## What is it
This server is a layer between all servers and the [*PushNotificationService*](https://github.com/RTUITLab/PushNotificationService) server. It receives all messages from servers to analyze them and make decisions: to send or not, if to send then how etc. 

## Content
- Requirements
- Configuration
- Boot
- How to start docker containers
    - Dev Build (Local)
        - Configuration
        - Run Docker
- Instruction how to push messages to the server
    - Reports
    - Comments (to reports)
    - Events and accepts

## Requirements
- Java 1.8 and later
- Gradle (Gredlew will choose the version himself)
- PostgreSQL 13 (May be in container)
- Redis (Container `redis:6.2.3-alpine`)

## Configuration
- Go to `./src/main/resources/application.yml`
- Set auth token in manually or add env variables
```yaml
secrets:
  token: ${ITLAB_NOTIFY_AUTH_URL:YOUR_TOKEN}
```
- If you use not default values also set another variables manually or by adding env variables (By default: DB name is `notify`, redis password is `admin`)

## Boot
- Boot postgreSQL and redis
- In project directory enter in terminal `gradle run`

## How to start containers
![Docker](https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Docker_%28container_engine%29_logo.svg/220px-Docker_%28container_engine%29_logo.svg.png)
### Dev build (Local)
#### Configuration
- Go to docker-compose.override.yml file
- Add valid auth token in 
```yaml
services:
  notify:
    environment:
      - ITLAB_NOTIFY_AUTH_TOKEN=YOUR_TOKEN
```
**This token is required to request users info from https://dev.manage.rtuitlab.dev/api/user/all**
#### Run Docker
Enter in terminal
- `docker-compose pull`
- `docker-compose -up -d`

## Instruction how to push messages to the server

All messages should be json objects

### Reports
How to send notification to a user that another person wrote a report about him

(The server checks the information if the user wrote a report about himself)
- push message info to `reports` channel in redis
- json structure:
```json
{
    "senderId" : "UUID of sender (String)",
    "receiverId" : "UUID of receiver (String)"
}
```
- example: `{"senderId":"846e0ad1-a424-4893-aga4-c9cas8fb324e","receiverId":"745asd799-2g84-b1ba-6acb-08d61e2gjs65a"}`

### Comments (to reports)
How to send information to a user that  person wrote a comment on the report about him
- push message info to `comments` channel in redis
- json structure:
```json
{
    "sender" : "Name Surname (String)",
    "report" : "Report name (String)",
    "userId" : "UUID of receiver (String)"
}
```
- example: `{"senderId":"846e0ad1-a424-4893-aga4-c9cas8fb324e","report":"Отчет как я провел это лето", "userId":"sd7af8ga-fa8a-hdf8-ns8d-ngdag890asdl"}`

### Events and accepts
#### Event message
How to send information about new event. The server will send notifications to all invitees.
If there are free spaces at the event, then will send notification to all users
- push message info to `events` channel in redis
- json structure of event message:
```json
{
    "title" : "Name of event (String)",
    "date" : "Date of event (String)",
    "size" : "Amount of spaces (Integer)",
    "invitedIds" : "List of UUID of invited people (List<String>)"
}
```
- example: `{"title":"Делегация из Урюпинска","date":"31.02.2021","size":23, "invitedIds":["sd7af8ga-fa8a-hdf8-ns8d-ngdag890asdl","846e0ad1-a424-4893-aga4-c9cas8fb324e"]}`

#### Accept message
How to send information to the server that the user has accepted the invitation
- push message info to `events` channel in redis
- *WARNING* You should add `accept` keyword with space and then json object!
- json structure of accept message:
```json
{
    "invitedId" : "UUID of sender of accept (String)",
    "event" : "Name of event (String)"
}
```
- example: `accept {"invitedId":"sd7af8ga-fa8a-hdf8-ns8d-ngdag890asdl","event":"Делегация из Урюпинска"}`