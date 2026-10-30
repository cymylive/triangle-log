package com.cymylive.trianglelog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
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

private data class TimelineRow(val date: String, val goal: Goal?, val done: Boolean, val note: String)

@Composable
fun TimelineScreen(state: AppState) {
    val goals = state.activeGoals
    var filter by remember { mutableStateOf<String?>(null) }

    val rows = remember(state.data, filter) {
        val recs = state.data.records
            .filter { filter == null || it.goalId == filter }
        val byDate = recs.groupBy { it.date }.toSortedMap(compareByDescending { it })
        val out = ArrayList<TimelineRow>()
        byDate.forEach { (date, list) ->
            list.sortedByDescending { it.updatedAt }.forEach { r ->
                val g = state.data.goals.firstOrNull { it.id == r.goalId }
                out.add(TimelineRow(date, g, r.done, r.note))
            }
        }
        out
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("我的记录", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Row {
            Chip("全部", filter == null) { filter = null }
            goals.forEach { g ->
                Spacer(Modifier.width(8.dp))
                Chip(g.name, filter == g.id) { filter = g.id }
            }
        }
        Spacer(Modifier.height(16.dp))

        if (rows.isEmpty()) {
            EmptyHint("还没有记录。去「今日」页点一下「完成」，这里就会长出来。")
        } else {
            LazyColumn {
                items(rows) { row ->
                    RowItem(row)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentDim,
            selectedLabelColor = Accent,
            containerColor = Surface1,
            labelColor = TextSub
        )
    )
}

@Composable
private fun RowItem(row: TimelineRow) {
    Row(Modifier.fillMaxWidth().background(Surface1, RoundedCornerShape(14.dp)).padding(14.dp)) {
        Box(
            Modifier.padding(top = 6.dp).size(10.dp).background(
                if (row.done) Accent else Surface2, CircleShape
            )
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(Dates.pretty(row.date), color = TextSub, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (row.done) "完成" else "未完成",
                    color = if (row.done) Accent else Danger,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                row.goal?.let { g -> if (g.dailyAction.isNotBlank()) g.dailyAction else g.name } ?: "已删除的目标",
                color = TextMain, fontSize = 15.sp
            )
            if (row.note.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("卡在：" + row.note, color = TextSub, fontSize = 13.sp)
            }
        }
    }
}
