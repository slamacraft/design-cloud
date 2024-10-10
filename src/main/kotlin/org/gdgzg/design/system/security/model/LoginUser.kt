package org.gdgzg.design.system.security.model

import org.gdgzg.design.module.entity.UserEntity
import java.time.LocalDateTime

class LoginUser {
    // 用户信息
    lateinit var userEntity: UserEntity

    // 登录信息
    lateinit var token: String
    lateinit var loginTime: LocalDateTime
    lateinit var expireTime: LocalDateTime
    lateinit var ip: String  // 登录ip
    lateinit var location: String    // 登录地点
    lateinit var browser: String // 浏览器类型
    lateinit var os: String  // 操作系统
}