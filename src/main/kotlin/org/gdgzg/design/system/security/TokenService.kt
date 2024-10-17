package org.gdgzg.design.system.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import jakarta.servlet.http.HttpServletRequest
import org.gdgzg.design.config.property.TokenProperty
import org.gdgzg.design.system.security.model.LoginUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

/**
 * 用户token缓存的前缀
 */
private const val USER_TOKEN_KEY_PREFIX = "user_token:"

@Component
class TokenService {
    @Autowired
    lateinit var tokenProperty: TokenProperty

    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, LoginUser>

    fun getLoginUser(request: HttpServletRequest): LoginUser? {
        return request.getToken()?.let {
            val tokenClaims = parseToken(it)
            val tokenCacheKey = tokenClaims[USER_TOKEN_KEY_PREFIX] as String
            redisTemplate.opsForValue().get(tokenCacheKey)
        }
    }

    fun HttpServletRequest.getToken(): String? = getHeader(tokenProperty.header)?.removePrefix("Bearer ")

    fun cacheLoginUser(loginUser: LoginUser){
        redisTemplate.opsForValue().set(
            USER_TOKEN_KEY_PREFIX + loginUser.token,
            loginUser,
            tokenProperty.expireTime.toLong(),
            java.util.concurrent.TimeUnit.MINUTES
        )
    }

    private fun parseToken(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(tokenProperty.secret)
            .parseClaimsJws(token)
            .body
    }
}