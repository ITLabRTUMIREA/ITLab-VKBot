# Notify analyzer for IT Lab system

Status | master | develop
------ | ------ | -------
Build  | [![Build Status](https://dev.azure.com/rtuitlab/RTU%20IT%20Lab/_apis/build/status/ITLab/ITLab-Notify?branchName=master)](https://dev.azure.com/rtuitlab/RTU%20IT%20Lab/_build/latest?definitionId=165&branchName=master) | [![Build Status](https://dev.azure.com/rtuitlab/RTU%20IT%20Lab/_apis/build/status/ITLab/ITLab-Notify?branchName=develop)](https://dev.azure.com/rtuitlab/RTU%20IT%20Lab/_build/latest?definitionId=165&branchName=develop) |

## Что это
Это сервер, который является прослойкой между всеми серверами и [*PushNotificationService*](https://github.com/RTUITLab/PushNotificationService) сервером. Он принимаем все сообщения от серверов, анализирует и принимает решения: отправлять или нет, а если отправлять то, как и т. д.

## Содержание
- Требования
- Конфигурация
- Запуска
- Как запустить докер контейнеры
    - Версия для разработки (Локальная)
        - Конфигурация
        - Запуска Докера
- Инструкция как отправлять сообщения на сервер
    - Отчеты
    - Комментарии (на отчеты)
    - События и подтверждения

## Требования
- Java 1.8 и позже
- Gradle (Gredlew сам определит необходимую версию)
- PostgreSQL 13 (Может быть в контейнере)
- Redis (Контейнер `redis:6.2.3-alpine`)

## Конфигурация
- Перейдите в `./src/main/resources/application.yml`
- Установите токен авторизации вручную или через переменную окружения
```yaml
secrets:
  token: ${ITLAB_NOTIFY_AUTH_URL:YOUR_TOKEN}
```
- Если вы используете не стандартные значения, то также установите и другие переменные в этом файле или установите их как переменные окружения (По умолчанию: имя БД - `notify`, пароль redis'a - `admin`)

## Запуск
- Запустить postgreSQL и redis
- В директории проекта введите в терминал `gradle run`

## Как запустить контейнеры
![Docker](https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Docker_%28container_engine%29_logo.svg/220px-Docker_%28container_engine%29_logo.svg.png)
### Версия для разработки (Локальная)
#### Конфигурация
- Перейдите в to docker-compose.override.yml файл
- Добавить правильный токен авторизации в
```yaml
services:
  notify:
    environment:
      - ITLAB_NOTIFY_AUTH_TOKEN=YOUR_TOKEN
```
**Этот токен требуется, чтобы совершать запросы для получения информации о пользователях по адресу https://dev.manage.rtuitlab.dev/api/user/all**
#### Запуск докера
Введите в терминал
- `docker-compose pull`
- `docker-compose -up -d`

## Инструкция как отправлять сообщения на сервер

Все сообщения должны быть в виде json объектов

### Отчеты
Как отправить уведомление пользователю, что другой человек написал о нем отчет

(Сервер проверяет информацию, если пользователь написал отчет о себе)
- отправить сообщение на канал `reports` в redis
- структура json:
```json
{
    "senderId" : "UUID отправителя (String)",
    "receiverId" : "UUID получателя (String)"
}
```
- пример: `{"senderId":"846e0ad1-a424-4893-aga4-c9cas8fb324e","receiverId":"745asd799-2g84-b1ba-6acb-08d61e2gjs65a"}`

### Комментарии к отчетам
Как отправить информацию пользователю, что человек написал комментарий на отчет о нем
- отправить сообщение на канал `comments` в redis
- структура json:
```json
{
    "sender" : "Имя Фамилия (String)",
    "report" : "Название отчета (String)",
    "userId" : "UUID получателя (String)"
}
```
- пример: `{"senderId":"846e0ad1-a424-4893-aga4-c9cas8fb324e","report":"Отчет как я провел это лето", "userId":"sd7af8ga-fa8a-hdf8-ns8d-ngdag890asdl"}`

### События и подтверждения
#### Сообщение о событии
Как отправить информацию о новом событии. Сервер отправит уведомления всем приглашенным.
Если есть свободные места, тогда отправит уведомление всем пользователям.
- отправить сообщение на канал `events` в redis
- структура сообщения о событии на json:
```json
{
    "title" : "Название мероприятие (String)",
    "date" : "Дата проведения (String)",
    "size" : "Кол-во мест (Integer)",
    "invitedIds" : "Список UUID приглашенных (List<String>)"
}
```
- пример: `{"title":"Делегация из Урюпинска","date":"31.02.2021","size":23, "invitedIds":["sd7af8ga-fa8a-hdf8-ns8d-ngdag890asdl","846e0ad1-a424-4893-aga4-c9cas8fb324e"]}`

#### Сообщение подтверждения
Как отправить информацию серверу, что пользователь подтвердил свое участие на событии
- отправить сообщение на канал `events` в redis
- *ПРЕДУПРЕЖДЕНИЕ* Вам нужно добавить ключевое слово `accept` с проделом после и после json объект!
- структура подтверждающего сообщения на json:
```json
{
    "invitedId" : "UUID отправителя подтверждения (String)",
    "event" : "Название мероприятия (String)"
}
```
- пример: `accept {"invitedId":"sd7af8ga-fa8a-hdf8-ns8d-ngdag890asdl","event":"Делегация из Урюпинска"}`