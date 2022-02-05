package com.example.tcomtest

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tcomtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val serviceConnection = CallServiceConnection()
    private var connection: TComConnection? = null
    private val tcomManager: TComManager by lazy { TComManager(applicationContext) }
    private lateinit var binding: ActivityMainBinding
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= 31) arrayOf(
        Manifest.permission.READ_PHONE_NUMBERS
    ) else arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dropBtn.setOnClickListener { closeConnection() }

        binding.activateBtn.setOnClickListener {
            val conn = connection
            if (conn != null) {
                conn.setActive()
            } else {
                Toast.makeText(applicationContext, "there is no call", Toast.LENGTH_SHORT).show()
            }
        }

        binding.incomingBtn.setOnClickListener {
            if (!hasPermissions()) {
                requestPermissions(requiredPermissions, 1)
                Toast.makeText(applicationContext, "don't have permissions", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            try {
                connection?.let {
                    closeConnection()
                }

                if (tcomManager.registerAccount()) {
                    tcomManager.addIncomingCall()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "account isn't registered",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.outgoingBtn.setOnClickListener {
            if (!hasPermissions()) {
                requestPermissions(requiredPermissions, 1)
                Toast.makeText(this, "don't have permissions", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            try {
                connection?.let {
                    closeConnection()
                }

                if (tcomManager.registerAccount()) {
                    tcomManager.addOutgoingCall()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "account isn't registered",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "failed to create an outgoing call - ${e.message}")
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
        binding.stateLabel.text = stateText
    }

    private fun addConnection(newConnection: TComConnection) {
        newConnection.listener = {
            val stateText = "state: ${Connection.stateToString(it)}"
            binding.stateLabel.text = stateText
        }
        connection = newConnection
        val stateText = "state: ${Connection.stateToString(newConnection.state)}"
        binding.stateLabel.text = stateText
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

    companion object {
        const val TAG = "MainActivity"
    }
}
