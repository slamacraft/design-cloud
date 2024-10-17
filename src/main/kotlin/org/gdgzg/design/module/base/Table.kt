@file:Suppress("UNUSED_EXPRESSION")

package org.gdgzg.design.module.base

import cn.hutool.core.util.StrUtil
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import java.time.LocalDateTime
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure


/**
 * 对exposed的Table进行扩展，假如一些类似于mybatis-plus的默认方法
 */
open abstract class Table<E : Entity<E>>(tableName: String) : LongIdTable(tableName), TypeReference {
    private val referencedKotlinType: KType by lazy { findSuperclassTypeArgument(javaClass.kotlin) }
    private var logicDeletedField: Column<Boolean>? = null

    /**
     * 类似于mybatis-plus的getById方法
     */
    fun getById(pid: Long): E? = selectAll().where { id eq pid }.notLogicDelete()  // 未逻辑删除
        .firstOrNull()?.let {
            buildEntity(it)
        }

    fun listByIds(ids: List<Long>): List<E> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        return selectAll().where { id inList ids }.notLogicDelete().map {
                buildEntity(it)
            }
    }

    /**
     * 将查询结果[ResultRow]转换为表对应的实体的方法
     */
    fun buildEntity(resultRow: ResultRow): E {
        val entity = createEntity()
        columns.forEach { column ->
            val value = resultRow[column]
            val fieldName = StrUtil.toCamelCase(column.name)
            if (value is EntityID<*>) {
                entity.toSet(fieldName, value.value)
            } else {
                entity.toSet(fieldName, value)
            }
        }
        return entity
    }

    /**
     * 批量新增
     */
    open fun insert(entity: E): E = EntityInsertStatement(this, false).insert(entity)

    /**
     * 批量新增
     */
    open fun batchInsert(entityList: Collection<E>) {
        if (entityList.isEmpty()) {
            return
        }
        batchInsert(entityList) { entity ->
            val row = this
            columns.forEach { column ->
                entity.toGet(StrUtil.toCamelCase(column.name))?.let {
                    row.serValue(column as Column<Any>, it)
                }
            }
        }
    }

    open fun batchUpdateById(entityList: List<E>) {
        entityList.forEach(this::updateById)
    }

    open fun updateById(entity: E) {
        update({ id eq entity.id() }) {
            entity.fields().forEach { (column, value) ->
                it.serValue(column, value)
            }
        }
    }

    /**
     * 通过id删除行
     */
    open fun removeById(pid: Long) {
        val idColumn = this.id
        if (logicDeletedField == null) {
            deleteWhere { idColumn eq pid }
        } else {
            update({ idColumn eq pid }) {
                it[logicDeletedField!!] = true
            }
        }
    }

    /**
     * 批量删除行
     */
    open fun removeByIds(ids: Collection<Long>) {
        if (ids.isEmpty()) {
            return
        }
        deleteWhere { this.id inList ids }
    }

    // ================== 一些为了让代码更顺畅的拓展函数
    private fun <E : Entity<E>> E.fields(): Map<Column<*>, Any?> {
        return columns.associateWith { this[StrUtil.toCamelCase(it.name)] }
    }

    private fun <E : Entity<E>> E.id(): Long = this["id"] as Long

    /**
     * 逻辑删除字段
     */
    protected fun Column<Boolean>.logicDelete() {
        logicDeletedField = this
    }

    fun Query.notLogicDelete(): Query = where(logicDeletedField != null) { logicDeletedField!! eq true }

    // =================== 下面是一些 操作符重载
    /**
     * 将表的操作转变为类似于集合操作的操作符重载，例如新增就是 UserTable += userEntity
     */
    operator fun plusAssign(entity: E) {
        insert(entity)
    }

    /**
     * 像操作集合一样批量新增
     */
    operator fun plusAssign(entity: Collection<E>) {
        batchInsert(entity)
    }


    private fun createEntity(): E = Entity.create(referencedKotlinType.jvmErasure) as E
}


open abstract class BaseTable<E>(tableName: String) : Table<E>(tableName) where E : BaseEntity, E : Entity<E> {

    val createBy = long("create_by").default(1114951452)

    val createTime = datetime("create_time").default(LocalDateTime.now())

    val updateBy = long("update_by").default(1114951452)

    val updateTime = datetime("update_time").default(LocalDateTime.now())
}

/**
 * //////////////// 下面是一些为了抽象exposed接口的拓展函数 ////////////////
 */

/**
 * 设置插入值
 */
private fun <T, E : Any> InsertStatement<E>.serValue(it: Column<*>, value: T?) {
    if (value != null) {
        this[it as Column<T>] = value
    }
}

/**
 * 设置插入值
 */
private fun <T> UpdateStatement.serValue(it: Column<*>, value: T?) {
    if (value != null) {
        this[it as Column<T>] = value
    }
}

fun Query.where(condition: Boolean, predicate: SqlExpressionBuilder.() -> Op<Boolean>): Query {
    return if (condition) {
        where(predicate)
    } else {
        this
    }
}
