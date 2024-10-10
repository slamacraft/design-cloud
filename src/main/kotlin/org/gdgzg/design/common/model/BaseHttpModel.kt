package org.gdgzg.design.common.model

data class RespWrapper(
    var code: Int = 0,
    var data: Any? = null,
    var msg: String? = null,
    var detail: String? = null
)