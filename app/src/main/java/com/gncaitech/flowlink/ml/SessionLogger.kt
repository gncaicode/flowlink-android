package com.gncaitech.flowlink.ml

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionLogger(context: Context, kind: String) {
    private val file: File
    private var writer: FileWriter? = null
    private val startTime = System.currentTimeMillis()

    init {
        val dir = File(context.getExternalFilesDir(null), "session_logs")
        dir.mkdirs()
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        file = File(dir, "flowlink_${kind}_$ts.txt")
        writer = FileWriter(file, false)
        append("SESSION: 시작 kind=$kind")
    }

    /** 로그 한 줄 기록. 백그라운드 스레드에서 호출 가능. */
    fun append(line: String) {
        val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
        synchronized(this) {
            try {
                writer?.write("[%8.3f] %s\n".format(elapsed, line))
                writer?.flush()
            } catch (_: Exception) {}
        }
    }

    /** 운동 종료 시 파일을 닫는다. 이후 append()는 무시된다. */
    fun close() {
        synchronized(this) {
            try {
                writer?.write("[%8.3f] SESSION: 종료\n".format(
                    (System.currentTimeMillis() - startTime) / 1000.0))
                writer?.close()
                writer = null
            } catch (_: Exception) {}
        }
    }

    /** 파일 공유용 Intent 반환. close() 후에도 사용 가능. */
    fun shareIntent(context: Context): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

/** ResultScreen에서 마지막 세션 로그를 공유할 수 있도록 보관하는 전역 홀더 */
object SessionLogHolder {
    @Volatile var last: SessionLogger? = null
}
