package com.example.hikeg

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.hikeg.dataclasses.AHike
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class HikeActivity : AppCompatActivity() {

    private lateinit var  startBtn : FloatingActionButton
    private lateinit var addLocBtn  : FloatingActionButton
    private lateinit var removeLocBtn  : FloatingActionButton
    private lateinit var gpsView : TextView
    private lateinit var timeView  : TextView
    private lateinit var checkpointView  : TextView
    private lateinit var distanceView  : TextView
    private lateinit var currentGpsView  : TextView
    private val receivedLocations: MutableList<Location> = mutableListOf()
    private val journeyLocations: MutableList<Location> = mutableListOf()
    private var isStarted : Boolean = false
    private var isOriginAdded : Boolean = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationsRequest: LocationRequest
    private var totalDistanceTraveled: Double = 0.0
    private lateinit var journeyName : String
    private lateinit var auth: FirebaseAuth
    private lateinit var journeyStartedTime : HashMap<String,Int>
    private lateinit var journeyEndTime : HashMap<String,Int>
    //private lateinit var speechRecognizer : SpeechRecognizer
    //private var isActivated: Boolean = false
   // private val activationKeyword: String = "save checkpoint"
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hike)
        auth = Firebase.auth

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        startBtn = findViewById(R.id.fabHikeControl)
        addLocBtn = findViewById(R.id.fabAddLocation)
        removeLocBtn = findViewById(R.id.fabAddRemoveLastLocation)
        gpsView = findViewById(R.id.textViewGPSCoordinateHike)
        timeView = findViewById(R.id.textViewStartTimeHike)
        checkpointView = findViewById(R.id.textViewLocCountHike)
        distanceView = findViewById(R.id.textViewDistanceTraveledHike)
        currentGpsView = findViewById(R.id.textViewCurrentGPSCoordinateHike)

       // speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)


        startBtn.setOnClickListener {
            if (isStarted){
                stopHandler()
                startBtn.setImageResource(R.drawable.baseline_my_location_24)
            }else {
                isStarted = true
                startHandler()
            }
        }


        addLocBtn.setOnClickListener {
            if (journeyLocations.isNotEmpty() && receivedLocations.isNotEmpty()){
                if (isLocationEnabled()){
                    val currentLocation = receivedLocations.last()
                    journeyLocations.add(currentLocation)
                    checkpointView.text = "Number of Checkpoints : ${journeyLocations.size} "
                    gpsView.text = "GPS Lat: ${currentLocation.latitude}  Long: ${currentLocation.longitude}"
                    updateTravelDistance()

                }else{
                    Toast.makeText(this, "Please Enable the Location Service", Toast.LENGTH_SHORT).show()
                }
            }
        }

        removeLocBtn.setOnClickListener {
            if (receivedLocations.isNotEmpty()){
                AlertDialog.Builder(this)
                    .setTitle("Confirm the Removal")
                    .setMessage("Remove the last Checkpoint ?")
                    .setPositiveButton("Yes") { _, _ ->
                        receivedLocations.removeLast()
                        checkpointView.text = "Number of Checkpoints : ${journeyLocations.size} "
                        Toast.makeText(this, "Checkpoint Removed", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No") { _, _ ->
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }else{
                Toast.makeText(this, "No Checkpoints Left", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private var isListening = false




    private fun saveData(callback: (Boolean) -> Unit) {
        val userID = auth.currentUser!!.uid

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = GsonBuilder().registerTypeAdapter(Location::class.java, LocationTypeAdapter()).create()

        val hikeRecord = AHike(userID, journeyName, journeyStartedTime, journeyEndTime, totalDistanceTraveled, getCurrentDateTimeSnapshot(), journeyLocations)
        val array1Json = gson.toJson(hikeRecord)

        Log.d("debug1", "$hikeRecord")
        Log.d("debug2", "$array1Json")
        editor.putString(journeyName, array1Json)
        editor.apply()
        callback(true)
    }



    private fun getCurrentDateTimeSnapshot(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentDateTime.format(formatter)
    }

    private fun updateTravelDistance() {
        totalDistanceTraveled = 0.0

        if (journeyLocations.size < 2) {
            distanceView.text = "Total Distance Traveled: 0 Km"
            return
        }

        for (i in 1 until journeyLocations.size) {
            val location1 = journeyLocations[i - 1]
            val location2 = journeyLocations[i]
            totalDistanceTraveled += location1.distanceTo(location2)
        }
        distanceView.text = "Total Distance Traveled:  ${String.format("%.2f", totalDistanceTraveled/1000)} Km"
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            (LocationManager.NETWORK_PROVIDER)
        )
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation() {
        Toast.makeText(this, "Waiting for the location", Toast.LENGTH_LONG).show()
        locationsRequest = LocationRequest()
        locationsRequest.priority =
            LocationRequest.PRIORITY_HIGH_ACCURACY
        locationsRequest.interval = 10000
        locationsRequest.fastestInterval = 8000
        locationsRequest.numUpdates = 200
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        fusedLocationProviderClient.requestLocationUpdates(
            locationsRequest, locationCallback, Looper.myLooper()
        )

    }

    private val locationCallback = object : LocationCallback() {
        @SuppressLint("SuspiciousIndentation", "SetTextI18n")
        override fun onLocationResult(p0: LocationResult) {
            val currentLocation = p0.lastLocation
            if (currentLocation != null) {
                receivedLocations.add(currentLocation)
                currentGpsView.text = "Current GPS Lat: ${currentLocation.latitude}  Long: ${currentLocation.longitude}"
                if (!isOriginAdded) {

                    gpsView.visibility = View.VISIBLE
                    timeView.visibility = View.VISIBLE
                    checkpointView.visibility = View.VISIBLE
                    distanceView.visibility = View.VISIBLE
                    currentGpsView.visibility = View.VISIBLE

                    removeLocBtn.visibility = View.VISIBLE
                    addLocBtn.visibility = View.VISIBLE
                    startBtn.setImageResource(R.drawable.baseline_share_location_24)
                    journeyLocations.add(currentLocation)
                    checkpointView.text = "Number of Checkpoints : ${journeyLocations.size} "
                    isOriginAdded = true
                }
            }
        }
    }

    private fun stopHandler() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Hike Destination")
            .setMessage("Are you ready to end the hike?")
            .setPositiveButton("Yes") { _, _ ->
                endHikeHandler()
                Toast.makeText(this, "Hike Ended", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { _, _ ->
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun endHikeHandler() {
        timeHandler(false)
        addLocBtn.visibility = View.GONE
        removeLocBtn.visibility = View.GONE
    }

    private fun startHandler() {
        if (checkPermission()) {
            if (isLocationEnabled()){
                AlertDialog.Builder(this)
                    .setTitle("Confirm Hike Origin")
                    .setMessage("Are you ready to start the hike?")
                    .setPositiveButton("Yes") { _, _ ->
                        getHikeName()
                    }
                    .setNegativeButton("No") { _, _ ->
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }else{
                Toast.makeText(this, "Please Enable the Location Service", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermissions()
        }
    }

    private fun getHikeName() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Enter Hike Name")
        builder.setMessage("Enter a name for the hike ")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Ok"){ _, _ ->
            val name = input.text.toString()
            if (name.isEmpty()){
                Toast.makeText(this, "Please enter a name ", Toast.LENGTH_SHORT).show()
            }else{
                journeyName = name
                getNewLocation()
                timeHandler(true)
                Toast.makeText(this, "Hike Started", Toast.LENGTH_SHORT).show()
            }

        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun timeHandler( isStart : Boolean) {
        val currentTime = java.util.Calendar.getInstance()
        val hours = currentTime.get(java.util.Calendar.HOUR_OF_DAY)
        val minutes = currentTime.get(java.util.Calendar.MINUTE)

        if (isStart){
            journeyStartedTime  = hashMapOf(
                "hours" to hours,
                "minutes" to minutes
            )
            timeView.text = "Started Time is : $hours h $minutes m"

        }else{
            journeyEndTime  = hashMapOf(
                "hours" to hours,
                "minutes" to minutes
            )
            saveData{
                if (it) {
                    val intent = Intent(this, HikerHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 111
        )
    }

}


//    private fun startVoiceRecognition() {
//
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 222)
//        } else {
//            val voiceCommand = "save checkpoint"
//
//            // Define a function to start listening for voice commands.
//            val startListening = {
//                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
//                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
//                speechRecognizer.startListening(intent)
//            }
//
//            // Start listening when not already listening.
//            if (!isListening) {
//                isListening = true
//                speechRecognizer.startListening(intent)
//            }
//
//            speechRecognizer.setRecognitionListener(object : RecognitionListener {
//                override fun onReadyForSpeech(params: Bundle?) {
//
//                }
//
//                override fun onBeginningOfSpeech() {
//
//                }
//
//                override fun onRmsChanged(rmsdB: Float) {
//
//                }
//
//                override fun onBufferReceived(buffer: ByteArray?) {
//
//                }
//                // Implement the RecognitionListener methods as before.
//
//                override fun onEndOfSpeech() {
//                    isListening = false
//                    startListening() // Restart listening when recognition ends.
//                }
//
//                override fun onError(error: Int) {
//
//                }
//
//                override fun onResults(results: Bundle?) {
//                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                    val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
//                    Log.d("debug","results are  $matches")
//                    if (matches != null) {
//                        if (isActivated) {
//                            isActivated = false
//                            stopRecognition()
//                        } else {
//                            matches.firstOrNull { it.contains(other = activationKeyword, ignoreCase = true) }
//                                ?.let {
//                                    isActivated = true
//                                }
//                            startVoiceRecognition()
//                        }
//                    }
//                }
//
//                override fun onPartialResults(partialResults: Bundle?) {
//                    val matches = partialResults?.getStringArrayList(RecognizerIntent.EXTRA_RESULTS)
//                    Log.d("debug","Matches $matches")
//                    if (!matches.isNullOrEmpty()) {
//                        val spokenText = matches[0].toLowerCase()
//                        if (spokenText.contains(voiceCommand)) {
//                            addCheckpoint()
//                        }
//                    }
//                }
//
//                override fun onEvent(eventType: Int, params: Bundle?) {
//
//                }
//            })
//        }
//    }


//    private fun addCheckpoint() {
//        if (journeyLocations.isNotEmpty() && receivedLocations.isNotEmpty()) {
//            if (isLocationEnabled()) {
//                val currentLocation = receivedLocations.last()
//                journeyLocations.add(currentLocation)
//                checkpointView.text = "Number of Checkpoints : ${journeyLocations.size} "
//                gpsView.text = "GPS Lat: ${currentLocation.latitude}  Long: ${currentLocation.longitude}"
//                updateTravelDistance()
//            } else {
//                Toast.makeText(this, "Please Enable the Location Service", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

//
//    private fun stopRecognition() {
//        speechRecognizer.stopListening()
//        speechRecognizer.destroy()
//    }
