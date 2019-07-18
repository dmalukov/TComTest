package com.example.tcomtest

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.util.Log


class TComService : ConnectionService() {
    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        Log.e(TAG, "onCreateIncomingConnectionFailed")
    }

    override fun onCreateIncomingConnection(
        phoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.i(TAG, "onCreateIncomingConnection - handle=$phoneAccount, request=$request")

        val connection = TComConnection(applicationContext)
        bindService(Intent(applicationContext, CallService::class.java), CallServiceConnection(connection), 0)
        return connection
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        Log.e(TAG, "onCreateOutgoingConnectionFailed")
    }

    override fun onCreateOutgoingConnection(
        handle: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.i(TAG, "onCreateOutgoingConnection - handle=$handle, request=$request")
        val connection = TComConnection(applicationContext)
        bindService(Intent(applicationContext, CallService::class.java), CallServiceConnection(connection), 0)
        return connection
    }

    inner class CallServiceConnection(private val tcomConnection: TComConnection) : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val callSrvBinder = binder as CallService.CallServiceBinder
            callSrvBinder.getCallService().addConnection(tcomConnection)
            unbindService(this)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    companion object {
        const val TAG = "TComService"
    }
}
