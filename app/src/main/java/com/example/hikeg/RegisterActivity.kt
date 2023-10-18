package com.example.hikeg

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.hikeg.dataclasses.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        val registerBtn = findViewById<Button>(R.id.userRegBtn)
        val userEmail =findViewById<EditText>(R.id.editTextTextRegisterEmailAddress)
        val userName = findViewById<EditText>(R.id.editTextTextRegisterName)
        val userPassword = findViewById<EditText>(R.id.editTextTextRegisterPassword)
        val userPasswordRetype = findViewById<EditText>(R.id.editTextTextRegisterPasswordConfirm)
        val userIDNum = findViewById<EditText>(R.id.editTextTextRegisterIDNumber)


        registerBtn.setOnClickListener {

            val email = userEmail.text.toString()
            val password = userPassword.text.toString()
            val passwordRetype = userPasswordRetype.text.toString()
            val name = userName.text.toString()
            val userID = userIDNum.text.toString()

            if (validation(name, email, userID, password, passwordRetype)) {

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener() { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser

                            val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                            user?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { profileTask ->
                                    if (profileTask.isSuccessful) {
                                        Log.d(ContentValues.TAG, "User profile updated. ${user.displayName}")
                                    }
                                }

                            if (user != null) {
                                addUserToDatabase(user.uid,name, email, userID)
                            }

                            Toast.makeText(this, "Registered successfully...", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(this, "Registration failed...", Toast.LENGTH_SHORT).show()

                        }
                    }

            }
        }
    }



    private fun addUserToDatabase (  uid: String,
                                     name: String,
                                     email: String,
                                     personID: String,
                                 ) : Boolean {
        return try{

            FirebaseDatabase.getInstance().reference.child("Users").child(uid).setValue(User(uid,name,email,personID)).addOnCompleteListener {
                Toast.makeText(this, "Registered successfully.", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Registered failed. ${it.message}", Toast.LENGTH_LONG).show()
            }

            false
        }catch (e : java.lang.Exception){
            false
        }
    }







    private fun validation(
        name: String,
        email: String,
        personID: String,
        password: String,
        passRetype: String,
    ): Boolean {
        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$")

        return if (name == "" || email =="" || personID =="" || personID=="" || password =="" || passRetype == ""){
            Toast.makeText(this, "Please fill all the fields...", Toast.LENGTH_LONG).show()

            false
        }else if(password.length < 6 ){
            Toast.makeText(this, "Password must be at least six characters long...", Toast.LENGTH_LONG).show()

            false
        }else if(password != passRetype){
            Toast.makeText(this, "Passwords doesn't match...", Toast.LENGTH_LONG).show()

            false
        }else if(!email.matches(emailRegex)){
            Toast.makeText(this, "Email is not in a valid format...", Toast.LENGTH_LONG).show()
            false
        }else if(name.length < 4 || personID.length < 5 ){
            Toast.makeText(this, "ID numbers and Names must have at least four characters...", Toast.LENGTH_LONG).show()
            false
        }
        else  {
            true
        }
    }
}


