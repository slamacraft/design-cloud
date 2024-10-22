package org.jdjzj.design.system.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 登录请求
 */
data class LoginReq(
    @Schema(description = "用户名")
    val userName: String,
    @Schema(description = "密码")
    val password: String,
    @Schema(description = "验证码")
    val code: String? = null,
    @Schema(description = "验证码唯一标识")
    val uuid: String? = null
)