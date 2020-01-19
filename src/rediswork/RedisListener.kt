package rediswork

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import utils.Config
import java.lang.Exception
import com.google.gson.Gson
import database.HibernateUtil
import messageprocessing.responses.EventChange
import messageprocessing.responses.EventConfirm
import messageprocessing.responses.EventNew
import messageprocessing.responses.event.EventView
import messageprocessing.responses.event.NotifyType
import workwithapi.RequestsToServerApi
import kotlin.concurrent.thread


class RedisListener(
    private val databaseConnection: HibernateUtil,
    private val requestsToServerApi: RequestsToServerApi
) {
    var jedis: Jedis? = null
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun eventHandling(eventView: EventView) {

        when (NotifyType.values()[eventView.type]) {
            NotifyType.EventNew -> {
                logger.info("New Event Notification Request")
                thread {
                    EventNew(eventView).process(
                        requestsToServerApi = requestsToServerApi,
                        databaseConnection = databaseConnection
                    )
                }
            }
            NotifyType.EventChange -> {
                logger.info("Modified Event Notification Request")
                thread {
                    EventChange(eventView).process(
                        requestsToServerApi = requestsToServerApi,
                        databaseConnection = databaseConnection
                    )
                }

            }
            NotifyType.EventConfirm -> {
                logger.info("Request for notification of participation in the event")
                thread {
                    EventConfirm(eventView).process(
                        requestsToServerApi = requestsToServerApi,
                        databaseConnection = databaseConnection
                    )
                }
            }
        }
    }

    fun unsubscribe() {
        logger.info("Redis closing subscribe")
        jedis!!.close()
        jedis = null
        logger.info("Redis subscribe closed")
    }

    fun listenEvents() {
        val password = Config().loadPath("database.redis.password")
        val port = Config().loadPath("database.redis.port")?.toInt()
        val host = Config().loadPath("database.redis.host")
        val chanel = Config().loadPath("database.redis.chanel")

        if (password != null && port != null && host != null && chanel != null) {
            try {
                jedis = Jedis(host, port)
                jedis!!.auth(password)
                logger.info("Connected to redis database!")
                val chanelName = Config().loadPath("database.redis.chanel")
                val jedisPubSub = object : JedisPubSub() {
                    override fun onMessage(channel: String, message: String?) {
                        logger.info("Channel $channel has sent a message")
                        if (!chanelName.isNullOrEmpty() && chanelName == chanel && !message.isNullOrEmpty()) {
                            val eventView = Gson().fromJson(message, EventView::class.java)
                            eventHandling(eventView)
                        }
                    }

                    override fun onSubscribe(channel: String?, subscribedChannels: Int) {
                        logger.info("We are subscribed to channel : " + channel!!)
                    }

                }

                jedis!!.subscribe(jedisPubSub, chanelName)
            } catch (ex: Exception) {
                logger.error(ex.message)
            } finally {
                jedis?.close()
            }
        } else {
            logger.error(
                "Check database.redis.password, database.redis.port, " +
                        "database.redis.host, database.redis.chanel fields in config file"
            )
        }

    }


}