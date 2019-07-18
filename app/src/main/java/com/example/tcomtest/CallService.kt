package com.example.tcomtest

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder


class CallService : Service() {
    var connectionListener: (TComConnection) -> Unit = {}

    override fun onBind(intent: Intent?): IBinder? {
        return CallServiceBinder()
    }

    fun addConnection(newConnection: TComConnection) {
        connectionListener(newConnection)
    }

    inner class CallServiceBinder : Binder() {
        fun getCallService(): CallService {
            return this@CallService
        }
    }
}