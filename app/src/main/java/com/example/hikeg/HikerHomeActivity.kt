package com.example.hikeg

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HikerHomeActivity : AppCompatActivity() {

    private lateinit var  recordHikeBtn : FloatingActionButton
    private lateinit var viewAllBtn  : FloatingActionButton
    private lateinit var profileBtn  : FloatingActionButton
    private lateinit var logOutBtn  : FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hiker_home)

        recordHikeBtn = findViewById(R.id.fabRecordHike)
        viewAllBtn  = findViewById(R.id.fabViewAllHikes)
        profileBtn  = findViewById(R.id.fabHikerProfile)
        logOutBtn   = findViewById(R.id.fabHikerLogout)

        logOutBtn.setOnClickListener {
            Firebase.auth.signOut()
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        recordHikeBtn.setOnClickListener {
            val intent = Intent(this, HikeActivity::class.java)
            startActivity(intent)
        }

        profileBtn.setOnClickListener {
            val intent = Intent(this, ViewHikeLocalActivity::class.java)
            startActivity(intent)
        }

    }
}