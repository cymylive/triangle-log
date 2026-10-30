package com.cymylive.trianglelog.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.cymylive.trianglelog.data.AppData
import com.cymylive.trianglelog.data.Dates
import com.cymylive.trianglelog.data.DayRecord
import com.cymylive.trianglelog.data.Goal
import com.cymylive.trianglelog.data.Importer
import com.cymylive.trianglelog.data.Store
import java.util.UUID

/** 全局状态：数据 + 操作，全部由它统一管理并落盘 */
class AppState(app: Application) : AndroidViewModel(app) {

    private val ctx get() = getApplication<Application>()

    var data: AppData by mutableStateOf(Store.load(ctx))
        private set

    val today: String get() = Dates.today()

    val activeGoals: List<Goal> get() = data.goals.filter { !it.archived }

    private fun commit(newData: AppData) {
        data = newData
        Store.save(ctx, newData)
    }

    fun reload() {
        data = Store.load(ctx)
    }

    /** 从备份文本导入，成功返回 null，失败返回错误信息 */
    fun importFromText(text: String): String? {
        val err = Importer.importFromText(ctx, text)
        if (err == null) {
            Store.invalidate()
            data = Store.load(ctx)
        }
        return err
    }

    // ---------- 目标 ----------

    fun addGoal(name: String, bigGoal: String, dueDate: String, weekTarget: String, dailyAction: String) {
        val g = Goal(
            id = UUID.randomUUID().toString(),
            name = name,
            bigGoal = bigGoal,
            dueDate = dueDate,
            weekTarget = weekTarget,
            dailyAction = dailyAction,
            createdAt = System.currentTimeMillis()
        )
        commit(data.copy(goals = data.goals + g))
    }

    fun updateGoal(g: Goal) {
        commit(data.copy(goals = data.goals.map { if (it.id == g.id) g else it }))
    }

    fun archiveGoal(id: String) {
        commit(data.copy(goals = data.goals.map { if (it.id == id) it.copy(archived = true) else it }))
    }

    // ---------- 每日记录 ----------

    fun recordFor(date: String, goalId: String): DayRecord? =
        data.records.firstOrNull { it.date == date && it.goalId == goalId }

    fun toggleDone(date: String, goalId: String) {
        val existing = recordFor(date, goalId)
        val records = when {
            existing == null -> data.records + DayRecord(
                date = date, goalId = goalId, done = true, updatedAt = System.currentTimeMillis()
            )
            else -> data.records.map {
                if (it.date == date && it.goalId == goalId)
                    it.copy(done = !it.done, updatedAt = System.currentTimeMillis())
                else it
            }
        }
        commit(data.copy(records = records))
    }

    fun setNote(date: String, goalId: String, note: String) {
        val existing = recordFor(date, goalId)
        val records = if (existing == null) {
            data.records + DayRecord(
                date = date, goalId = goalId, done = false, note = note,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            data.records.map {
                if (it.date == date && it.goalId == goalId)
                    it.copy(note = note, updatedAt = System.currentTimeMillis())
                else it
            }
        }
        commit(data.copy(records = records))
    }
}
