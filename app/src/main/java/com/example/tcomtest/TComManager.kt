package com.example.tcomtest

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.core.content.getSystemService


class TComManager(private val context: Context) {
    private val tm: TelecomManager = context.getSystemService()!!

    fun registerAccount(): Boolean {
        val accountHandle = getAccountHandle()
        var phoneAccount = tm.getPhoneAccount(accountHandle)
        if (phoneAccount == null) {
            val builder = PhoneAccount.builder(accountHandle, BuildConfig.APPLICATION_ID)
            builder.setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
            phoneAccount = builder.build()
            tm.registerPhoneAccount(phoneAccount)
        }
        return true
    }

    private fun getAccountHandle(): PhoneAccountHandle {
        val phoneAccountLabel = BuildConfig.APPLICATION_ID
        val componentName = ComponentName(context, TComService::class.java)
        return PhoneAccountHandle(componentName, phoneAccountLabel)
    }

    fun addIncomingCall() {
        val extras = Bundle()
        extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, getAccountHandle())
        extras.putString(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, "12356")
        tm.addNewIncomingCall(getAccountHandle(), extras)
    }

    @SuppressLint("MissingPermission")  // CALL_PHONE is not required for self-managed ConnectionService
    fun addOutgoingCall() {
        val extras = Bundle()
        extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, getAccountHandle())
        tm.placeCall(Uri.parse("tel:123456"), extras)
    }
}