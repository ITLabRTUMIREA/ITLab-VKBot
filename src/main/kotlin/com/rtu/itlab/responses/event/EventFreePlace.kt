//package com.rtu.itlab.responses.event
//
//import com.google.gson.JsonObject
//import com.rtu.itlab.database.DBClient
//
///**
// * Class of sending a message to VC, when empty seats appeared on an event
// * @param tmp - Json info about event
// * @param db - Database with persons info
// */
//class EventFreePlace(tmp: JsonObject?, db: DBClient) : EventInfo(tmp, db) {
//
//    override fun send() {
//        vk.messages()
//                .send(actor, userIds)
//                .message("На событие «${eventTitle}» появились свободные места!" +
//                        "\nКоличество свободных мест: $participantsCount" +
//                        "\nНачало: $beginDate $beginTime" +
//                        "\nОкончание: $endDate $endTime" +
//                        "\nАдрес проведения мероприятия: $address" +
//                        "\nСсылка на событие: $url")
//                .execute()
//    }
//}