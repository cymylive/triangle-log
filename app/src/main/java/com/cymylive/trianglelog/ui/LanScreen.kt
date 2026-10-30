package com.cymylive.trianglelog.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cymylive.trianglelog.web.NetUtils
import com.cymylive.trianglelog.web.ServerHolder

@Composable
fun LanScreen(state: AppState) {
    val ctx = LocalContext.current
    var running by remember { mutableStateOf(ServerHolder.running) }
    var ip by remember { mutableStateOf(NetUtils.localIp()) }
    var pendingText by remember { mutableStateOf<String?>(null) }
    var msg by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val text = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
            if (text.isNullOrBlank()) {
                msg = "文件读取为空"
            } else {
                pendingText = text
            }
        } catch (e: Exception) {
            msg = "读取失败：" + (e.message ?: "")
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("局域网与备份", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("手机和电脑连同一个 WiFi，电脑浏览器打开下面的地址，就能看记录、导出或导入备份。", color = TextSub, fontSize = 13.sp)
        Spacer(Modifier.height(20.dp))

        Column(Modifier.fillMaxWidth().background(Surface1, RoundedCornerShape(16.dp)).padding(18.dp)) {
            Text("服务状态", color = TextSub, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text(if (running) "已开启" else "未开启", color = if (running) Accent else TextSub, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            if (running) {
                Spacer(Modifier.height(14.dp))
                Text("电脑浏览器打开", color = TextSub, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text("http://" + (ip ?: "手机IP") + ":" + ServerHolder.PORT.toString(), color = Accent, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                if (running) {
                    ServerHolder.stop()
                    running = false
                } else {
                    ip = ServerHolder.start(ctx)
                    running = ServerHolder.running
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (running) Surface2 else Accent,
                contentColor = if (running) TextMain else Bg
            )
        ) {
            Text(if (running) "停止服务" else "开启服务", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(18.dp))
        Column(Modifier.fillMaxWidth().background(Surface1, RoundedCornerShape(16.dp)).padding(18.dp)) {
            Text("数据备份", color = TextSub, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Text("从备份文件导入", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text("选择之前导出的 JSON 文件，会覆盖当前所有数据。导入前会自动存一份旧的。", color = TextSub, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { picker.launch(arrayOf("application/json", "text/plain", "*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("选择备份文件", color = Accent) }
        }

        if (msg != null) {
            Spacer(Modifier.height(14.dp))
            Text(msg!!, color = Danger, fontSize = 13.sp)
        }

        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth().background(Surface1, RoundedCornerShape(16.dp)).padding(18.dp)) {
            Text("说明", color = TextSub, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text("· 局域网可看、可导出、可上传导入", color = TextSub, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text("· 手机 IP 可能变，连不上就重新开一次服务看新地址", color = TextSub, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text("· 同一 WiFi 下才可用，公共 WiFi 若开了隔离会连不上", color = TextSub, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text("· 导入会覆盖现有数据，操作前自动备份一份旧的", color = TextSub, fontSize = 13.sp)
        }
    }

    if (pendingText != null) {
        AlertDialog(
            onDismissRequest = { pendingText = null },
            containerColor = Surface1,
            title = { Text("确认导入？", color = TextMain) },
            text = {
                Column {
                    Text("导入会覆盖当前全部目标和记录。", color = TextMain, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("导入前会自动存一份旧数据在 App 目录，出问题可找回。", color = TextSub, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val err = state.importFromText(pendingText!!)
                    msg = if (err == null) "导入成功" else err
                    pendingText = null
                }) { Text("确认导入", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = { pendingText = null }) { Text("取消", color = TextSub) }
            }
        )
    }
}
