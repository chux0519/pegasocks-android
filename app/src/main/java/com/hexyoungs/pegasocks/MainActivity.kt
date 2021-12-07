package com.hexyoungs.pegasocks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var nl = NativeLibs()
        nl.printNativeLibsVersion();

        setContentView(R.layout.main_activity)
    }
}