package trik.testsys.webclient.service.security.login

import java.io.Serializable

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface SessionData : Serializable {

    /**
     * Sets all fields to null.
     *
     * @author Roman Shishkin
     * @since 2.0.0
     */
    fun invalidate()
}