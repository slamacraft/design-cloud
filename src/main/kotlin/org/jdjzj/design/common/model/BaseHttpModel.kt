package org.jdjzj.design.common.model

/**
 * 响应对象的包装类，返回数据都按这个格式来
 */
data class RespWrapper(
    var code: Int = 0,
    var data: Any? = null,
    var msg: String? = null,
    var detail: String? = null
)

data class PlainText(
    var text: String
)