package com.cymylive.trianglelog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cymylive.trianglelog.data.Goal

@Composable
fun GoalsScreen(state: AppState) {
    var showAdd by remember { mutableStateOf(false) }
    val goals = state.activeGoals

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("我的目标", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (goals.isEmpty()) {
            EmptyHint("还没有目标。点下面「新建目标」，先定一个靶心。")
            Spacer(Modifier.height(16.dp))
        }

        goals.forEach { g ->
            GoalItem(g)
            Spacer(Modifier.height(14.dp))
        }

        Button(
            onClick = { showAdd = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)
        ) { Text("新建目标", fontWeight = FontWeight.Bold) }
    }

    if (showAdd) {
        AddGoalDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, big, due, week, daily ->
                state.addGoal(name, big, due, week, daily)
                showAdd = false
            }
        )
    }
}

@Composable
private fun GoalItem(g: Goal) {
    Column(
        Modifier.fillMaxWidth().background(Surface1, RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        Text("目标 · " + g.name, color = Accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        if (g.bigGoal.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(g.bigGoal, color = TextMain, fontSize = 14.sp)
        }
        if (g.dueDate.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text("截止：" + g.dueDate, color = TextSub, fontSize = 13.sp)
        }
        if (g.weekTarget.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text("周靶", color = TextSub, fontSize = 12.sp)
            Text(g.weekTarget, color = TextMain, fontSize = 14.sp)
        }
        if (g.dailyAction.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text("日动作", color = TextSub, fontSize = 12.sp)
            Text(g.dailyAction, color = TextMain, fontSize = 14.sp)
        }
    }
}

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var big by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var week by remember { mutableStateOf("") }
    var daily by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface1,
        title = { Text("新建目标", color = TextMain) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Field("目标名（如：教资上岸）", name) { name = it }
                Spacer(Modifier.height(10.dp))
                Field("大目标描述（可空）", big) { big = it }
                Spacer(Modifier.height(10.dp))
                Field("截止日期（如 2027-03）", due) { due = it }
                Spacer(Modifier.height(10.dp))
                Field("本周靶（如：看完第1-2章）", week) { week = it }
                Spacer(Modifier.height(10.dp))
                Field("每日动作（如：背5个要点）", daily) { daily = it }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), big.trim(), due.trim(), week.trim(), daily.trim()) },
                enabled = name.isNotBlank()
            ) { Text("创建", color = Accent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = TextSub) }
        }
    )
}

@Composable
private fun Field(placeholder: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = TextSub, fontSize = 13.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentDim,
            unfocusedBorderColor = Surface2,
            focusedTextColor = TextMain,
            unfocusedTextColor = TextMain,
            cursorColor = Accent
        )
    )
}
