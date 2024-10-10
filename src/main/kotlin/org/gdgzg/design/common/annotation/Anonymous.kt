package org.gdgzg.design.common.annotation

annotation class Anonymous(
    val type: AnonymousType = AnonymousType.ALL
)

enum class AnonymousType {
    ALL,
    Anonymous
}