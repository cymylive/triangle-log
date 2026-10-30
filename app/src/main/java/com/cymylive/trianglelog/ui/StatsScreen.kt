package com.cymylive.trianglelog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cymylive.trianglelog.data.Dates
import com.cymylive.trianglelog.data.Goal
import com.cymylive.trianglelog.data.Logic
import java.time.LocalDate

@Composable
fun StatsScreen(state: AppState) {
    val year = LocalDate.now().year
    val allStats = Logic.stats(state.data)
    val goals = state.activeGoals
    var pickedDate by remember { mutableStateOf<String?>(null) }

    val heat = remember(state.data) {
        val dates = Logic.doneDates(state.data)
        val map = HashMap<String, Int>()
        dates.forEach { map[it] = 1 }
        map
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text(year.toString(), color = TextSub, fontSize = 14.sp)
        Text("这一年", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("点击方格查看当天记录", color = TextSub, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))

        Heatmap(year, heat) { d -> pickedDate = d }
        Spacer(Modifier.height(20.dp))

        Column(Modifier.fillMaxWidth().background(Surface1, RoundedCornerShape(16.dp)).padding(18.dp)) {
            StatLine("今年做了", allStats.yearDays.toString() + " 天")
            Spacer(Modifier.height(10.dp))
            StatLine("累计记录", allStats.totalDays.toString() + " 天")
            Spacer(Modifier.height(10.dp))
            StatLine("当前连续", allStats.currentStreak.toString() + " 天")
            Spacer(Modifier.height(10.dp))
            StatLine("最长连续", allStats.maxStreak.toString() + " 天")
        }

        if (goals.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Text("按目标", color = TextSub, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            goals.forEach { g ->
                GoalStat(state, g)
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("数据都在本机，可在「局域网」页导出备份。", color = TextSub, fontSize = 12.sp)
    }

    pickedDate?.let { d ->
        val recs = state.data.records.filter { it.date == d }
        AlertDialog(
            onDismissRequest = { pickedDate = null },
            containerColor = Surface1,
            title = { Text(Dates.pretty(d), color = TextMain) },
            text = {
                Column {
                    if (recs.isEmpty()) {
                        Text("这天没有记录。", color = TextSub, fontSize = 14.sp)
                    } else {
                        recs.forEach { r ->
                            val g = state.data.goals.firstOrNull { it.id == r.goalId }
                            Text(
                                (g?.name ?: "已删除目标") + " · " + (if (r.done) "完成" else "未完成"),
                                color = if (r.done) Accent else Danger,
                                fontSize = 14.sp
                            )
                            if (r.note.isNotBlank()) {
                                Text("  卡在：" + r.note, color = TextSub, fontSize = 12.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickedDate = null }) { Text("关闭", color = TextSub) }
            }
        )
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSub, fontSize = 14.sp)
        Text(value, color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun GoalStat(state: AppState, g: Goal) {
    val s = Logic.stats(state.data, g.id)
    Column(Modifier.fillMaxWidth().background(Surface1, RoundedCornerShape(14.dp)).padding(14.dp)) {
        Text(g.name, color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("做了 " + s.totalDays.toString() + " 天", color = TextMain, fontSize = 13.sp)
            Text("连续 " + s.currentStreak.toString() + " 天", color = TextSub, fontSize = 13.sp)
        }
    }
}

/** 年度热力图：横向滚动，每列一周（周一在上），可点击 */
@Composable
private fun Heatmap(year: Int, done: Map<String, Int>, onPick: (String) -> Unit) {
    val start = LocalDate.of(year, 1, 1)
    val offset = start.dayOfWeek.value - 1
    val days = (0 until 371).map { start.plusDays(it.toLong()) }.filter { it.year == year }
    val scroll = rememberScrollState()

    Row(Modifier.fillMaxWidth().horizontalScroll(scroll)) {
        for (w in 0 until 54) {
            Column {
                for (d in 0 until 7) {
                    val idx = w * 7 + d - offset
                    if (idx >= 0 && idx < days.size) {
                        val key = days[idx].toString()
                        val on = done.containsKey(key)
                        Box(
                            Modifier.padding(1.dp).size(10.dp)
                                .background(if (on) Accent else Surface2, RoundedCornerShape(2.dp))
                                .clickable { onPick(key) }
                        )
                    } else {
                        Spacer(Modifier.padding(1.dp).size(10.dp))
                    }
                }
            }
        }
    }
}
