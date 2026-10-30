package com.cymylive.trianglelog.web

import android.content.Context
import com.cymylive.trianglelog.data.Dates
import com.cymylive.trianglelog.data.Logic
import com.cymylive.trianglelog.data.Store
import fi.iki.elonen.NanoHTTPD

/** 局域网只读服务：电脑浏览器打开 http://手机IP:8765 即可查看与导出 */
class LocalWebServer(private val appContext: Context, port: Int = 8765) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri ?: "/"
        return when (uri) {
            "/", "/index.html" -> newFixedLengthResponse(
                Response.Status.OK, "text/html; charset=utf-8", htmlPage()
            )
            "/api/data" -> newFixedLengthResponse(
                Response.Status.OK, "application/json; charset=utf-8", Store.exportJson(appContext)
            )
            "/api/export" -> {
                val resp = newFixedLengthResponse(
                    Response.Status.OK, "application/json; charset=utf-8", Store.exportJson(appContext)
                )
                resp.addHeader("Content-Disposition", "attachment; filename=triangle-log-backup.json")
                resp
            }
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
        }
    }

    private fun esc(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    private fun htmlPage(): String {
        val data = Store.load(appContext)
        val stats = Logic.stats(data)
        val sb = StringBuilder()
        sb.append("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"utf-8\">")
        sb.append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
        sb.append("<title>三角日志</title><style>")
        sb.append("body{background:#121212;color:#ededed;font-family:-apple-system,Segoe UI,sans-serif;margin:0;padding:24px;}")
        sb.append("h1{font-size:22px;margin:0 0 6px;}h2{font-size:15px;color:#9a9a9a;font-weight:normal;margin:0 0 20px;}")
        sb.append(".card{background:#1e1e1e;border-radius:12px;padding:16px;margin-bottom:12px;}")
        sb.append(".row{display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid #2a2a2a;}")
        sb.append(".row:last-child{border-bottom:none;}")
        sb.append(".done{color:#7bd88f;}.miss{color:#e57373;}")
        sb.append(".muted{color:#9a9a9a;font-size:13px;}")
        sb.append(".btn{display:inline-block;background:#7bd88f;color:#0d1f12;padding:10px 18px;border-radius:8px;text-decoration:none;font-weight:bold;margin-top:12px;}")
        sb.append("</style></head><body>")
        sb.append("<h1>三角日志</h1><h2>只读视图 · 数据在手机上</h2>")

        sb.append("<div class=\"card\">")
        sb.append("<div class=\"row\"><span>累计记录</span><span>").append(stats.totalDays).append(" 天</span></div>")
        sb.append("<div class=\"row\"><span>当前连续</span><span>").append(stats.currentStreak).append(" 天</span></div>")
        sb.append("<div class=\"row\"><span>最长连续</span><span>").append(stats.maxStreak).append(" 天</span></div>")
        sb.append("</div>")

        sb.append("<div class=\"card\"><div class=\"muted\" style=\"margin-bottom:8px\">目标</div>")
        val goals = data.goals.filter { !it.archived }
        if (goals.isEmpty()) {
            sb.append("<div class=\"muted\">暂无目标</div>")
        } else {
            for (g in goals) {
                sb.append("<div class=\"row\"><span>").append(esc(g.name)).append("</span><span class=\"muted\">")
                sb.append(esc(g.dailyAction)).append("</span></div>")
            }
        }
        sb.append("</div>")

        sb.append("<div class=\"card\"><div class=\"muted\" style=\"margin-bottom:8px\">最近记录</div>")
        val recent = data.records.sortedByDescending { it.updatedAt }.take(60)
        if (recent.isEmpty()) {
            sb.append("<div class=\"muted\">暂无记录</div>")
        } else {
            for (r in recent) {
                val g = data.goals.firstOrNull { it.id == r.goalId }
                val label = if (g != null) g.name else "已删除目标"
                val cls = if (r.done) "done" else "miss"
                val txt = if (r.done) "完成" else "未完成"
                sb.append("<div class=\"row\"><span>").append(Dates.pretty(r.date))
                sb.append(" · ").append(esc(label)).append("</span><span class=\"").append(cls).append("\">").append(txt).append("</span></div>")
                if (r.note.isNotBlank()) {
                    sb.append("<div class=\"muted\" style=\"padding-bottom:6px\">卡在：").append(esc(r.note)).append("</div>")
                }
            }
        }
        sb.append("</div>")

        sb.append("<a class=\"btn\" href=\"/api/export\">导出备份 JSON</a>")
        sb.append("<p class=\"muted\" style=\"margin-top:20px\">手机与电脑需在同一 WiFi。数据只读，修改请在手机上操作。</p>")
        sb.append("</body></html>")
        return sb.toString()
    }
}
