package com.nn.interview.core.utils

import android.app.Application
import android.os.Handler
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.nn.interview.core.helper.ErrorReporting
import okhttp3.internal.http2.ConnectionShutdownException
import okhttp3.internal.http2.StreamResetException
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class CrashlyticsTree(val app: Application) : Timber.Tree() {
    val handler = Handler()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= Log.WARN) {
            handler.post { sendLogToCrashlytics(priority, tag, message, t) }
        }
    }

    private fun sendLogToCrashlytics(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        throwable?.let {
            when {
                it is ApiException -> Log.println(priority, tag, message)
                throwable is UnknownHostException ||
                throwable is SocketTimeoutException ||
                throwable is SSLHandshakeException ||
                throwable is StreamResetException ||
                throwable is ConnectionShutdownException -> Log.println(priority, tag, message)
                else -> ErrorReporting.sendException(priority, tag, message, throwable)
            }
        } ?: kotlin.run { ErrorReporting.sendException(priority, tag, message) }
    }
}