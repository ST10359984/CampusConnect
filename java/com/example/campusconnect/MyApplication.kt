package com.example.campusconnect

import android.app.Application
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val settings = firestoreSettings {
            setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
        }
        Firebase.firestore.firestoreSettings = settings
    }
}