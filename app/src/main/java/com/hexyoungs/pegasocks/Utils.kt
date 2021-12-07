package com.hexyoungs.pegasocks

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

const val PEGAS_DIR = "pegas"
const val PEGAS_CONFIG_FILE = "pegasrc"
const val PEGAS_LOG_FILE = "pegas.log"

fun loadPegasConfig(context: Context): String {
    val path = context.getFilesDir()
    val pegasDir = File(path, PEGAS_DIR)
    if(!pegasDir.exists()) {
        pegasDir.mkdirs()
    }
    val file = File(pegasDir, PEGAS_CONFIG_FILE)
    if(!file.exists()) {
        return ""
    }
    val inputAsString = FileInputStream(file).bufferedReader().use { it.readText() }
    return inputAsString
}

fun savePegasConfig(content: String, context: Context) {
    val path = context.getFilesDir()
    val pegasDir = File(path, PEGAS_DIR)
    if(!pegasDir.exists()) {
        pegasDir.mkdirs()
    }
    val file = File(pegasDir, PEGAS_CONFIG_FILE)
    FileOutputStream(file).use {
        it.write(content.toByteArray())
    }
}