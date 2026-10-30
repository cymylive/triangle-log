package com.cymylive.trianglelog.data

import kotlinx.serialization.Serializable

/** 一个目标：大目标 + 周靶 + 日动作，对应三角里的「定靶」 */
@Serializable
data class Goal(
    val id: String,
    val name: String,
    val bigGoal: String = "",
    val dueDate: String = "",
    val weekTarget: String = "",
    val dailyAction: String = "",
    val createdAt: Long = 0L,
    val archived: Boolean = false
)

/** 一条每日记录：做了没 + 卡在哪，对应三角里的「每天动」和「每天看」 */
@Serializable
data class DayRecord(
    val date: String,
    val goalId: String,
    val done: Boolean,
    val note: String = "",
    val updatedAt: Long = 0L
)

/** 全部数据，整体存成一个 JSON 文件，导出备份就是这个文件 */
@Serializable
data class AppData(
    val goals: List<Goal> = emptyList(),
    val records: List<DayRecord> = emptyList(),
    val version: Int = 1
)
