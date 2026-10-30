package com.cymylive.trianglelog.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
fun TodayScreen(state: AppState) {
    var viewDate by remember { mutableStateOf(state.today) }
    val goals = state.activeGoals
    val isToday = viewDate == state.today

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text(Dates.pretty(viewDate), color = TextSub, fontSize = 15.sp)
        Spacer(Modifier.height(4.dp))
        Text(if (isToday) "今天" else "补记", color = TextMain, fontSize = 30.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = {
                val d = Dates.parse(viewDate)
                if (d != null) viewDate = Dates.key(d.minusDays(1))
            }) { Text("← 前一天", color = TextSub, fontSize = 13.sp) }
            Spacer(Modifier.width(4.dp))
            if (!isToday) {
                TextButton(onClick = { viewDate = state.today }) {
                    Text("回到今天", color = Accent, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.width(4.dp))
            TextButton(
                onClick = {
                    val d = Dates.parse(viewDate)
                    if (d != null && viewDate < state.today) viewDate = Dates.key(d.plusDays(1))
                },
                enabled = viewDate < state.today
            ) { Text("后一天 →", color = if (viewDate < state.today) TextSub else Surface2, fontSize = 13.sp) }
        }

        Spacer(Modifier.height(20.dp))

        if (goals.isEmpty()) {
            EmptyHint("还没有目标，去「目标」页新建一个，给今天一个靶心。")
        }

        goals.forEach { goal ->
            GoalCard(state, goal, viewDate)
            Spacer(Modifier.height(16.dp))
        }

        if (isToday) {
            val streak = Logic.stats(state.data).currentStreak
            if (streak > 0) {
                Spacer(Modifier.height(8.dp))
                Text("连续 " + streak.toString() + " 天", color = Accent, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun GoalCard(state: AppState, goal: Goal, date: String) {
    val record = state.recordFor(date, goal.id)
    val done = record?.done == true
    var note by remember(goal.id, date) { mutableStateOf(record?.note ?: "") }

    Column(
        Modifier.fillMaxWidth().background(Surface1, RoundedCornerShape(18.dp)).padding(18.dp)
    ) {
        Text(goal.name, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))
        Text(
            if (goal.dailyAction.isNotBlank()) goal.dailyAction else "未设置今日动作",
            color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.SemiBold
        )
        if (goal.weekTarget.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text("本周：" + goal.weekTarget, color = TextSub, fontSize = 13.sp)
        }
        Spacer(Modifier.height(20.dp))

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(
                onClick = { state.toggleDone(date, goal.id) },
                shape = CircleShape,
                modifier = Modifier.size(if (done) 84.dp else 96.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (done) AccentDim else Accent,
                    contentColor = if (done) Accent else Bg
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (done) {
                        Icon(Icons.Filled.Check, contentDescription = "已完成", modifier = Modifier.size(30.dp))
                        Spacer(Modifier.height(2.dp))
                        Text("已完成", fontSize = 12.sp)
                    } else {
                        Text("完成", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = note,
            onValueChange = {
                note = it
                state.setNote(date, goal.id, it)
            },
            placeholder = { Text("卡在哪？（可不填）", color = TextSub, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentDim,
                unfocusedBorderColor = Surface2,
                focusedTextColor = TextMain,
                unfocusedTextColor = TextMain,
                cursorColor = Accent
            )
        )
    }
}

@Composable
fun EmptyHint(text: String) {
    Box(
        Modifier.fillMaxWidth().background(Surface1, RoundedCornerShape(16.dp)).padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = TextSub)
            Spacer(Modifier.size(8.dp))
            Text(text, color = TextSub, fontSize = 14.sp)
        }
    }
}
