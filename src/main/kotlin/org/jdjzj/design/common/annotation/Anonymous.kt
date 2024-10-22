package org.jdjzj.design.common.annotation

annotation class Anonymous(
    val type: org.jdjzj.design.common.annotation.AnonymousType = org.jdjzj.design.common.annotation.AnonymousType.ALL
)

enum class AnonymousType {
    ALL,
    Anonymous
}