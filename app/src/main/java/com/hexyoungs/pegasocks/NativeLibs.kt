package com.hexyoungs.pegasocks

import android.util.Log


class NativeLibs {
  private val TAG = "NativeLibs"
  companion object {
    init {
      System.loadLibrary("native-libs")
    }
  }

  external fun getPegasVersion(): String
  external fun startPegaSocks(configPath: String?, threads: Int): Boolean
  external fun stopPegaSocks()
  external fun startOutputPipe()


  fun printNativeLibsVersion() {
    var ver = getPegasVersion();
    Log.v(TAG, "pegasocks version: " + ver);
  }
}