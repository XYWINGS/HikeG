package com.example.hikeg.dataclasses

import android.location.Location

data class AHike(
    val author : String ,
    val hikeName:String,
    val timeStarted : HashMap<String,Int> ,
    val timeEnded : HashMap<String,Int> ,
    val distance: Double,
    val dateTimeCreated : String,
    val locationSet: MutableList<Location> = mutableListOf()
)