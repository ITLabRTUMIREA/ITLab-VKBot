package database

import database.schema.UserSettings
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistry
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.slf4j.LoggerFactory
import utils.Config

class HibernateUtil {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var sessionFactory: SessionFactory? = null
    private var registry: StandardServiceRegistry? = null

    /**
     * Setup session (connect) to database
     */
    fun setUpSession(): HibernateUtil {

        logger.debug("Connecting to postgres database")
        val configuration = Configuration()

        configuration.addAnnotatedClass(UserSettings::class.java)

        configuration.configure("hibernate.cfg.xml")

        if (Config("resources/secureInfo.conf").companion.config != null) {

            val username = Config().loadPath("database.postgres.username")
            if (username != null) configuration.setProperty("hibernate.connection.username", username)

            val password = Config().loadPath("database.postgres.password")
            if (password != null) configuration.setProperty("hibernate.connection.password", password)

            val url = Config().loadPath("database.postgres.url")
            if (url != null) configuration.setProperty("hibernate.connection.url", url)

        }

        registry = StandardServiceRegistryBuilder().applySettings(configuration.properties!!).build()

        try {
            sessionFactory = configuration.buildSessionFactory(registry)
            logger.info("Connection established")
        } catch (ex: Exception) {
            logger.error(ex.message)
            if (sessionFactory != null)
                StandardServiceRegistryBuilder.destroy(registry)
        }

        return this
    }

    /**
     * Close session
     */
    fun closeSession(): HibernateUtil {
        logger.info("Closing sessionFactory session")
        if (sessionFactory != null && sessionFactory!!.isOpen) {
            sessionFactory!!.close()
            logger.info("Session is closed")
        } else {
            logger.info("Session has already been closed")
        }
        return this
    }

    /**
     * Getting entities from table
     * @param classRef class reference T
     * @return List<T>
     */
    fun <T : Any> getEntities(classRef: T): List<T>? {
        var session: Session? = null

        if (sessionFactory == null || sessionFactory!!.isClosed)
            setUpSession()

        return try {
            session = sessionFactory!!.openSession()
            session.beginTransaction()

            val builder = session.criteriaBuilder
            val criteria = builder.createQuery(classRef::class.java)
            criteria.from(classRef::class.java)
            val userProperties = session.createQuery(criteria).resultList

            session.close()
            userProperties
        } catch (ex: Exception) {
            session?.close()
            logger.error(ex.message + " sessionFactory")
            null
        }
    }

    /**
     * Delete entity(ies) from table by id
     * @param id id of entity, default = null -> all entities
     * @param classRef  class reference T
     * @return boolean value: true if deleted else false
     */
    fun <T : Any> deleteEntities(id: String? = null, classRef: T): Boolean {
        var session: Session? = null

        if (sessionFactory == null || sessionFactory!!.isClosed)
            setUpSession()

        return try {
            session = sessionFactory!!.openSession()
            session.beginTransaction()
            if (!id.isNullOrBlank()) {
                session.delete(session.get(classRef::class.java, id))
            } else {
                getEntities(classRef)?.forEach {
                    session.delete(it)
                }
            }
            session.transaction.commit()
            session.close()

            true
        } catch (ex: Exception) {
            session?.close()
            logger.error(ex.message + " sessionFactory")
            false
        }
    }

    /**
     * Adding entities
     * @param classRef class reference T
     * @return true if added else false
     */
    fun <T : Any> addEntity(classRef: T): Boolean {
        var session: Session? = null

        if (sessionFactory == null || sessionFactory!!.isClosed)
            setUpSession()

        return try {
            session = sessionFactory!!.openSession()
            session.beginTransaction()


            session.save(classRef)
            session.transaction.commit()
            session.close()
            true
        } catch (ex: Exception) {
            session?.close()
            logger.error(ex.message + " sessionFactory")
            false
        }
    }

    /**
     * Getting entity by id
     * @param id
     * @param classRef
     * @return entity if we got else null
     */
    fun <T : Any> getEntityById(id: String, classRef: T): T? {
        var session: Session? = null

        if (sessionFactory == null || sessionFactory!!.isClosed)
            setUpSession()

        return try {
            session = sessionFactory!!.openSession()
            session.beginTransaction()

            val entity: T = session.load(classRef::class.java, id)

            session.close()
            entity
        } catch (ex: Exception) {
            session?.close()
            logger.error(ex.message + " sessionFactory")
            null
        }
    }

    /**
     * Updating entities
     * @param classRef class reference T
     * @return true if updated else false
     */
    fun <T : Any> updateEntity(classRef: T): Boolean {
        var session: Session? = null

        if (sessionFactory == null || sessionFactory!!.isClosed)
            setUpSession()

        return try {

            session = sessionFactory!!.openSession()
            session.beginTransaction()

            session.merge(classRef)
            session.evict(classRef)
            session.transaction.commit()
            session.close()
            true
        } catch (ex: Exception) {
            session?.close()
            logger.error(ex.message + " sessionFactory")
            false
        }
    }

    /**
     * Check if there is a user with given id in database
     * @param id
     * @return true if the user is found else false
     */
    fun isUserInDatabase(id: String): Boolean {
        val users = getEntities(UserSettings())
        if (!users.isNullOrEmpty()) {
            users.forEach {
                if (it.id == id)
                    return true
            }
        }
        return false
    }


}