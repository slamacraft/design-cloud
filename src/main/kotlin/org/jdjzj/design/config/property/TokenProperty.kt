package org.jdjzj.design.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "token")
class TokenProperty {
    // 令牌自定义标识
    var header: String? = null

    // 令牌秘钥
    var secret: String? = null

    // 令牌有效期，单位分钟（默认30分钟）
    var expireTime = 0L
    var priKey: String? = null
    var pubKey: String? = null

    var retryCount = 5 // 默认5次重试机会
    var retryInterval = 1 // 重试5次的间隔
    var lockTime = 10 // 锁定10分钟
}