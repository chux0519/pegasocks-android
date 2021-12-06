package com.hexyoungs.pegasocks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hexyoungs.pegasocks.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var nl = NativeLibs()
        nl.printNativeLibsVersion();

        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}