package com.example.calendario.data

import android.os.Handler
import android.os.Looper
import com.google.firebase.crashlytics.FirebaseCrashlytics

internal fun triggerCrashlyticsTest() {
    FirebaseCrashlytics.getInstance().log("Test crash requested from Alerts in the debug build.")
    Handler(Looper.getMainLooper()).post {
        throw RuntimeException("Crashlytics test crash (Calendar)")
    }
}
