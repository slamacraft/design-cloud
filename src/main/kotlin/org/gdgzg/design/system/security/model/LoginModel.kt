package org.gdgzg.design.system.security.model

/**
 * 登录请求
 */
data class LoginReq(
    val userName: String,
    val password: String,
    val code: String? = null,
    val uuid: String? = null
)