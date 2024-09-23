package trik.testsys.webclient.security.login

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.SessionScope
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.webclient.security.SessionData

/**
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Component
@SessionScope
class LoginData : SessionData {

    var accessToken: AccessToken? = null

    override fun invalidate() {
        accessToken = null
    }
}