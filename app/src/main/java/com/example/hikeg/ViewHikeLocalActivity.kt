package com.example.hikeg

import android.content.Context
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikeg.dataclasses.AHike
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class ViewHikeLocalActivity : AppCompatActivity() {

    private lateinit var hikeAdaptor: LocalHikeAdaptor
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_hike_local)

        recyclerView = findViewById(R.id.localHikeViewRecView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val gson = GsonBuilder().registerTypeAdapter(Location::class.java, LocationTypeAdapter()).create()
        val keys = sharedPreferences.all.keys

        val dataList = mutableListOf<AHike>()

        for (key in keys) {
            val jsonString = sharedPreferences.getString(key, null)
            if (!jsonString.isNullOrBlank()) {
                val type = object : TypeToken<AHike>() {}.type
                val hike = gson.fromJson<AHike>(jsonString, type)
                dataList.add(hike)
            }else{
                Toast.makeText(this,"No Hike Records Found", Toast.LENGTH_LONG).show()
            }
        }

        Log.d("debug3"," data lis tis $dataList")

        hikeAdaptor = LocalHikeAdaptor(dataList.toMutableList(),baseContext)
        recyclerView.adapter = hikeAdaptor
        hikeAdaptor.notifyDataSetChanged()

    }

//
}
