package org.gdgzg.design.common.component

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class JsonMapper(val objectMapper: ObjectMapper) {

    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        INSTANCE = this.objectMapper
    }

    companion object {
        private var INSTANCE: ObjectMapper? = null

        fun getInstance(): ObjectMapper {
            return INSTANCE!!
        }
    }
}

fun <T> T.toJson(): String = JsonMapper.getInstance().writeValueAsString(this)