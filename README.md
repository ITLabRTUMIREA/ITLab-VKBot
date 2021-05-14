# Notify analyzer for IT Lab system


Status | master | develop
--- | --- | ---
Build |  [![Build Status][build-master-image]][build-master-link] | [![Build Status][build-dev-image]][build-dev-link]

[build-dev-image]: https://dev.azure.com/rtuitlab/RTU%20IT%20Lab/_apis/build/status/ITLab-VKBot?branchName=develop
[build-dev-link]: https://dev.azure.com/rtuitlab/RTU%20IT%20Lab/_build/latest?definitionId=81&branchName=develop
[build-master-image]: https://dev.azure.com/rtuitlab/RTU%20IT%20Lab/_apis/build/status/ITLab-VKBot?branchName=master
[build-master-link]: https://dev.azure.com/rtuitlab/RTU%20IT%20Lab/_build/latest?definitionId=81&branchName=master

## Content
- How to start
    - Configuration
    - Run
    - Docker
        - Container
        - Compose
- Instruction how to push messages to the server
## How to start
### Configuration
- Go to ./src/main/resources/application.yml
- Add valid auth token in 
```yaml
secrets:
  token: ${ITLAB_NOTIFY_AUTH_TOKEN:"YOUR AUTH TOKEN"}
```
### Run
Enter `gradle run` in terminal (in IntelliJ IDEA press ctrl+enter)

### Docker
![Docker](https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Docker_%28container_engine%29_logo.svg/220px-Docker_%28container_engine%29_logo.svg.png)

Enter in terminal
- `docker-compose -up -d`

## Instruction how to push messages to the server

All messages should be json objects

### Report server
- push message info to `reports` channel in redis
- json structure:
```json
{
    "senderId" : "UUID of sender (String)",
    "receiverId" : "UUID of receiver (String)"
}
```
- example: `{"senderId":"846e0ad1-a424-4893-aga4-c9cas8fb324e","receiverId":"745asd799-2g84-b1ba-6acb-08d61e2gjs65a"}`

### Comment server
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

### Events server
#### Event message
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
- push message info to `events` channel in redis
- *WARNING* You should add `accept` keyword with space and then json object!
- json structure of event message:
```json
{
    "invitedId" : "UUID of sender of accept (String)",
    "event" : "Name of event (String)"
}
```
- example: `accept {"invitedId":"sd7af8ga-fa8a-hdf8-ns8d-ngdag890asdl","event":"Делегация из Урюпинска"}`