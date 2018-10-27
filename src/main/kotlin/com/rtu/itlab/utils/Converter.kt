package com.rtu.itlab.utils

/**
 * Method for converting Map<String, Any> to Map<String,String>
 * @param inputs Map<String,Any>
 * @return Map<String, String>
 */
fun mapAnyToMapString(inputs: Map<String, Any>): Map<String, String> {
    val result = hashMapOf<String, String>()
    for (input in inputs) {
        result.plus(Pair(input.key, input.value.toString()))
    }
    return result
}