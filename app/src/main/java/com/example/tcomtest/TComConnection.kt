package com.example.tcomtest

import android.content.Context
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log
import android.widget.Toast

class TComConnection(private val context: Context) : Connection() {
    var listener: (state: Int) -> Unit = {}

    init {
        connectionProperties = PROPERTY_SELF_MANAGED
    }

    override fun onShowIncomingCallUi() {
        super.onShowIncomingCallUi()
        Toast.makeText(context, "SHOW UI", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "onShowIncomingCallUi")
    }

    override fun onStateChanged(state: Int) {
        super.onStateChanged(state)
        Log.i(TAG, "onStateChanged, state=${stateToString(state)}")
        listener(state)
    }

    override fun onReject() {
        Log.i(TAG, "onReject")
        close()
    }

    override fun onDisconnect() {
        Log.i(TAG, "onDisconnect")
        close()
    }

    private fun close() {
        setDisconnected(DisconnectCause(DisconnectCause.CANCELED))
        destroy()
    }

    fun isClosed(): Boolean {
        return state == STATE_DISCONNECTED
    }

    companion object {
        const val TAG = "TComConnection"
    }
}