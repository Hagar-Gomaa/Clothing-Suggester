package com.example.clothingsuggester.utils

object Temperature {

    fun convertToCelsius(tem: Float): Float {
        return tem - 273.15f
    }
}