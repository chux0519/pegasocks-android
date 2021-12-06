package com.hexyoungs.pegasocks

class NativeLibs {
  companion object {
    // Used to load the 'native-lib' library on application startup.
    init {
      System.loadLibrary("native-libs")
    }
  }

  external fun getPegasVersion() : String
}
