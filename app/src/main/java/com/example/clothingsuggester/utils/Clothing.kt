package com.example.clothingsuggester.utils

import com.example.clothingsuggester.R

object Clothing {
    fun getSummerClothing(): List<List<Int>> {
        return listOf(
            listOf(
                R.drawable.sweatshirt_green,
                R.drawable.skirt_
            ),
            listOf(
                R.drawable.sweatshirt_gray,
                R.drawable.skirtr
            )
        )
    }

    fun getWinterClothing(): List<List<Int>> {
        return listOf(
            listOf(
                R.drawable.skirtr,
                R.drawable.topthree, R.drawable.jecketone
            ),
            listOf(
                R.drawable.skirt_,
                R.drawable.topfive, R.drawable.jecket_two
            ),
            listOf(
                R.drawable.skiry__,
                R.drawable.toptwo, R.drawable.jecket_three
            )

        )
    }
}