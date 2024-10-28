package org.jdjzj.design.system

import cn.hutool.core.lang.UUID
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.servlet.http.HttpServletRequest
import org.jdjzj.design.config.property.TokenProperty
import org.jdjzj.design.system.model.LoginUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * 用户token缓存的前缀
 */
private const val USER_TOKEN_KEY_PREFIX = "user_token:"
private const val USER_TOKEN_UUID = "user_token_uuid"

@Component
class TokenService {
    @Autowired
    lateinit var tokenProperty: TokenProperty
    @Autowired
    lateinit var redisTemplate: RedisTemplate<Any, Any>
//    @Autowired
//    lateinit var redisTemplate: RedisTemplate<String, LoginUser>
//
//    fun getLoginUser(request: HttpServletRequest): LoginUser? {
//        return request.getToken()?.let {
//            val tokenClaims = parseToken(it)
//            val tokenCacheKey = tokenClaims[USER_TOKEN_KEY_PREFIX] as String
//            redisTemplate.opsForValue().get(tokenCacheKey)
//        }
//    }

    fun HttpServletRequest.getToken(): String? = getHeader(tokenProperty.header)?.removePrefix("Bearer ")

    private fun parseToken(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(tokenProperty.secret)
            .parseClaimsJws(token)
            .body
    }

    fun createAndCacheToken(loginUser: LoginUser):String {
        // 这个是缓存在redis的登录凭证唯一键值
        val randomUUID = UUID.randomUUID(true)
        // 这个是给用户的token
        val token = Jwts.builder()
            .setClaims(mapOf(Pair(USER_TOKEN_UUID, randomUUID.node())))
            .signWith(SignatureAlgorithm.HS512, tokenProperty.secret)
            .compact()
        redisTemplate.opsForValue()
            .set(
                USER_TOKEN_KEY_PREFIX + randomUUID.node(), loginUser,
                tokenProperty.expireTime, TimeUnit.MINUTES
            )

        return token
    }
}