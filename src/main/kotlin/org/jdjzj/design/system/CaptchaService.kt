package org.jdjzj.design.system

import cn.hutool.core.util.IdUtil
import cn.hutool.log.LogFactory
import com.google.code.kaptcha.Producer
import jakarta.annotation.Resource
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * 验证码组件
 */
// 验证码的redis缓存前缀
private const val CAPTCHA_KEY_PREFIX = "captcha:"

val log = KotlinLogging.logger {}

@Component
class CaptchaService {
    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    lateinit var captchaProducer: Producer

    fun createCaptcha(uuid: String): String {
        val id = IdUtil.fastUUID()
        val text = captchaProducer.createText()
        log.debug { "验证码uuid: $id 验证码内容: $text" }
        // 验证码缓存2分钟
        redisTemplate.opsForValue()
            .set("$CAPTCHA_KEY_PREFIX$id", text, 2, TimeUnit.MINUTES)

        return text
    }

    fun getCaptcha(uuid: String): String {
        return redisTemplate.opsForValue().get("$CAPTCHA_KEY_PREFIX$uuid") ?: ""
    }

    fun checkCaptcha(uuid: String, code: String): Boolean {
        val captcha = getCaptcha(uuid)
        return captcha.isNotEmpty() && captcha == code
    }
}