package com.rtu.itlab.utils

/**
 * Method for converting Map<String, Any> to Map<String,String>
 * @param inputs Map<String,Any>
 * @return Map<String, String>
 */
fun mapAnyToMapString(inputs: Map<String, Any?>): Map<String, String> {
    val result = hashMapOf<String, String>()
    inputs.forEach {key,value->
        if(value != null)
            result[key] = value.toString()
        else
            result[key] = ""
    }
    return result
}