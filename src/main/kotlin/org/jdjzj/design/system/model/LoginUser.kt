package org.jdjzj.design.system.model

import org.jdjzj.design.module.entity.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

data class LoginUser(
    // 用户信息
    val userEntity: UserEntity,
) : UserDetails {
    lateinit var token: String
    lateinit var loginTime: LocalDateTime
    lateinit var expireTime: LocalDateTime
    // 登录信息
    lateinit var ip: String  // 登录ip
    lateinit var location: String    // 登录地点
    lateinit var browser: String // 浏览器类型
    lateinit var os: String  // 操作系统

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf()
    }

    override fun getUsername(): String = userEntity.username

    override fun getPassword(): String = userEntity.password
}