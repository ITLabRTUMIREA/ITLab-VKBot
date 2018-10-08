package com.rtu.itlab.utils

import java.io.IOException
import java.io.FileInputStream
import java.util.*

fun getProp():Properties{

    val fis: FileInputStream
    val properties = Properties()
    try {
        fis = FileInputStream("src/main/resources/config.properties")
        properties.load(fis)

    } catch (e: IOException) {
        print("Error loading properties ${e.message}")
    }
    return properties
}