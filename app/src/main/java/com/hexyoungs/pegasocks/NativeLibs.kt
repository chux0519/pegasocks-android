package com.hexyoungs.pegasocks

import android.util.Log;

class NativeLibs {
  private val TAG = "NativeLibs"

  companion object {
    // Used to load the 'native-lib' library on application startup.
    init {
      System.loadLibrary("native-libs")
    }
  }

  external fun getPegasVersion() : String
  external fun getTun2SocksVersion(): String

  fun printNativeLibsVersion() {
    var ver = getPegasVersion();
    Log.v(TAG, "pegasocks version: " + ver);
    ver = getTun2SocksVersion();
    Log.v(TAG, "tun2socks version: " + ver);
  }
}
