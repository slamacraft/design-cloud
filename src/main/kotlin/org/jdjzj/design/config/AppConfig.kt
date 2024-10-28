package org.jdjzj.design.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.code.kaptcha.Producer
import com.google.code.kaptcha.impl.DefaultKaptcha
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class AppConfig {

    @Bean
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        return objectMapper
    }

    @Bean
    fun kaptchaProducer(): Producer {
        return DefaultKaptcha()
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory?): RedisTemplate<Any, Any> {
        val template = RedisTemplate<Any, Any>()
        template.connectionFactory = connectionFactory

        val serializer = JsonRedisSerializer(Any::class.java)
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = serializer
        // Hash的key也采用StringRedisSerializer的序列化方式
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = serializer

        template.afterPropertiesSet()
        return template
    }
}

class JsonRedisSerializer<T>(
    val clazz: Class<T>
) : RedisSerializer<T> {

    private val objectMapper = ObjectMapper()
    override fun serialize(value: T?): ByteArray? = value?.let {
        objectMapper.writeValueAsString(value).toByteArray()
    }

    override fun deserialize(bytes: ByteArray?): T? = bytes?.let {
        objectMapper.readValue(it, clazz)
    }
}