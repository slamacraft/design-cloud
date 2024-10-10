package org.gdgzg.design.system.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import jakarta.servlet.http.HttpServletRequest
import org.gdgzg.design.config.property.TokenProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

private const val LOGIN_USER_KEY = "login_user_key"

@Component
class TokenService {
    @Autowired
    lateinit var tokenProperty: TokenProperty

    fun getLoginUser(request: HttpServletRequest) {
        request.getToken()?.let {
            val tokenClaims = parseToken(it)
            val uuid = tokenClaims[LOGIN_USER_KEY] as String
            uuid
        }
    }

    fun HttpServletRequest.getToken(): String? = getHeader(tokenProperty.header)?.removePrefix("Bearer ")

    private fun parseToken(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(tokenProperty.secret)
            .parseClaimsJws(token)
            .body
    }
}