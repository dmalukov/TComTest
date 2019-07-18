package com.example.tcomtest

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private val serviceConnection = CallServiceConnection()
    private var connection: TComConnection? = null
    private val tcomManager: TComManager by lazy { TComManager(applicationContext) }
    private val requiredPermissions =
        arrayOf(
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_CALL_LOG
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drop_btn.setOnClickListener { closeConnection() }

        answer_btn.setOnClickListener {
            val conn = connection
            if (conn != null) {
                conn.setActive()
            } else {
                Toast.makeText(applicationContext, "there is no call", Toast.LENGTH_SHORT).show()
            }
        }

        incoming_btn.setOnClickListener {
            if (!hasPermissions()) {
                requestPermissions(requiredPermissions, 1)
                Toast.makeText(applicationContext, "don't have permissions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                if (tcomManager.registerAccount()) {
                    tcomManager.addIncomingCall()
                } else {
                    Toast.makeText(applicationContext, "account isn't registered", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        outgoing_btn.setOnClickListener {
            if (!hasPermissions()) {
                requestPermissions(requiredPermissions, 1)
                Toast.makeText(applicationContext, "don't have permissions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                if (tcomManager.registerAccount()) {
                    tcomManager.addOutgoingCall()
                } else {
                    Toast.makeText(applicationContext, "account isn't registered", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        Intent(this, CallService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }


    override fun onStop() {
        super.onStop()
        closeConnection()
        unbindService(serviceConnection)
    }


    private fun hasPermissions(): Boolean {
        return requiredPermissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }


    private fun closeConnection() {
        connection?.let {
            if (!it.isClosed()) {
                it.setDisconnected(DisconnectCause(DisconnectCause.CANCELED))
            }
            it.listener = {}
        }

        val stateText = "state: no call"
        state_label.text = stateText
    }

    private fun addConnection(newConnection: TComConnection) {
        newConnection.listener = {
            val stateText = "state: ${Connection.stateToString(it)}"
            state_label.text = stateText
        }
        connection = newConnection
        val stateText = "state: ${Connection.stateToString(newConnection.state)}"
        state_label.text = stateText
    }


    inner class CallServiceConnection : ServiceConnection {
        private var callService: CallService? = null
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val callSrvBinder = binder as CallService.CallServiceBinder
            val service = callSrvBinder.getCallService()
            service.connectionListener = { addConnection(it) }
            callService = service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            callService?.connectionListener = {}
        }
    }
}
