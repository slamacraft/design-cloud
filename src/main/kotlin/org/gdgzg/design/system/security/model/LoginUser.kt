package org.gdgzg.design.system.security.model

import org.gdgzg.design.module.entity.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

data class LoginUser(

    // 用户信息
    var userEntity: UserEntity,

    // 登录信息
    var token: String,
    var loginTime: LocalDateTime,
    var expireTime: LocalDateTime,
    var ip: String,  // 登录ip
    var location: String,    // 登录地点
    var browser: String, // 浏览器类型
    var os: String  // 操作系统
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        TODO("Not yet implemented")
    }

    override fun getUsername(): String = userEntity.username

    override fun getPassword(): String = userEntity.password
}