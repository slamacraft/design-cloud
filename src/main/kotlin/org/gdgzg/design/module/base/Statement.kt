package org.gdgzg.design.module.base

import cn.hutool.core.util.StrUtil
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

/**
 * 用于定义插入时的行为
 */
open class EntityInsertStatement(
    table: LongIdTable,
    isIgnore: Boolean = false
) : InsertStatement<Int>(table, isIgnore) {

    /**
     * 在新增时执行的操作
     *
     * 先把entity的值赋值给表的字段，然后执行插入，再把插入得到值赋值给entity
     */
    fun <E : Entity<E>> insert(entity: E): E {
        table.columns.forEach {
            it.setValue(values, entity, table)
        }
        // 执行插入
        execute(TransactionManager.current())

        // 把插入得到的值再赋值给entity
        table.columns.forEach {
            entity.toSet(it.name, get(it))
        }
        return entity
    }

}

fun <E : Entity<E>> Column<*>.setValue(values: MutableMap<Column<*>, Any?>, entity: E, table: Table) {
    val value = entity.toGet(StrUtil.toCamelCase(name))
    if (value != null) {
        values[this] = value
    } else if (name != "id") {
        // 获取字段默认值
        val defaultValue = defaultValueFun?.invoke()

        // 校验字段可空性，如果没有默认值，且字段为不可空，抛出异常
        require(defaultValue != null || columnType.nullable) {
            "${table.tableName}字段${name}不能为空"
        }
        values[this] = defaultValue

        entity.toSet(StrUtil.toCamelCase(name), defaultValue)
    }
}