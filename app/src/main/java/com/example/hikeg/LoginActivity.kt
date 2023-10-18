package com.example.hikeg

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth

        val loginButton = findViewById<Button>(R.id.loginBtn)
        val loginEmail = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val loginPassword = findViewById<EditText>(R.id.editTextTextPassword)
        val signupButton = findViewById<Button>(R.id.regBtn)

        val user = auth.currentUser

        if (user!=null){
            val intent = Intent(this, HikerHomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        signupButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }


        loginButton.setOnClickListener {

                val email = loginEmail.text.toString()
                val password = loginPassword.text.toString()

                if (email == "" || password == "") {
                    Toast.makeText(this, "Please Enter Email and Password ", Toast.LENGTH_LONG)
                        .show()
                } else {

                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this, HikerHomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "No account found for the login details", Toast.LENGTH_LONG).show()
                        }
                    }
                }

        }



    }
}