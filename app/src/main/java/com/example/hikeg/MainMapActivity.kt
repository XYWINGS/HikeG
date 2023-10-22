package com.example.hikeg

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.hikeg.databinding.ActivityMainMapBinding
import com.google.android.gms.maps.MapsInitializer
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class MainMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainMapBinding
    private val hikeList : MutableList<DataSnapshot> = mutableListOf()
    private lateinit var autoCompleteTextView : AutoCompleteTextView
    private var  suggestionList : MutableList<String>  = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) {
        }

        binding = ActivityMainMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        autoCompleteTextView = findViewById(R.id.actvSearchHikes)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val searchQuery = editable.toString().lowercase()
                suggestionList.clear()
                // Extract hike names from DataSnapshot list
                for (dataSnapshot in hikeList) {
                    val hikeName = dataSnapshot.key.toString()
                    if (hikeName.lowercase().contains(searchQuery)) {
                        suggestionList.add(hikeName)
                    }
                }

                val adapter = ArrayAdapter(
                    this@MainMapActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    suggestionList
                )
                autoCompleteTextView.setAdapter(adapter)
            }

            override fun beforeTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }




    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sriLankaLatLng = LatLng(7.8731, 80.7718)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaLatLng, 8.0f))
        startThings()
    }

    private fun startThings() {
        getAllHikeRecords{
            if (it){
                Log.d("Debug" ,"hikedatalist  $hikeList-------------------------------------")
            }
        }
    }

    private fun getAllHikeRecords( callback: (Boolean) -> Unit) {
       val dataRef = FirebaseDatabase.getInstance("https://hikeg-168f2-default-rtdb.asia-southeast1.firebasedatabase.app").reference
            .child("HikeRecords")

        dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ownerSnapshot in dataSnapshot.children) {
                    for (hikeSnapshot in ownerSnapshot.children) {
                            hikeList.add(hikeSnapshot)
                        }
                }
                callback(true)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MainMapActivity, "Database Error ${databaseError.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        })
    }
}