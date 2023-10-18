package com.example.hikeg

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hikeg.dataclasses.AHike
import com.google.firebase.database.FirebaseDatabase


class LocalHikeAdaptor (private val record:MutableList<AHike> , private val context: Context): RecyclerView.Adapter<LocalHikeAdaptor.ViewHolder>() {

    private val colors = arrayOf("#E1BEE7","#D1C4E9", "#C5CAE9", "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9")

    class ViewHolder(itemView: View,val context: Context): RecyclerView.ViewHolder(itemView) {

        val hikeDateTimeText : TextView = itemView.findViewById(R.id.hikeDateTimeTextViewHiker)
        val hikeName: TextView = itemView.findViewById(R.id.hikeNameTextViewHiker)
        val deleteBtn: Button = itemView.findViewById(R.id.deleteHikeRecordHiker)
        val uploadBtn:Button = itemView.findViewById(R.id.uploadHikeRecordHiker)
        val distanceText : TextView = itemView.findViewById(R.id.hikeDistanceTextView)
        val recordLinearLayout : LinearLayout =itemView.findViewById(R.id.aLocalHikeRecordLayout)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position % colors.size]
        holder.recordLinearLayout.setBackgroundColor(Color.parseColor(color))
        val item = record[position]

        holder.hikeName.text = "Name : ${item.hikeName}"
        holder.hikeDateTimeText.text = "Date : ${item.dateTimeCreated}"
        holder.distanceText.text = "Distance Traveled : ${String.format("%.2f", item.distance/1000)} Km"

        holder.deleteBtn.setOnClickListener {
            removeFromLocalStorage(item)
        }

        holder.uploadBtn.setOnClickListener {
            if ( isInternetAvailable(context)){
                Log.d("debug","Imcalled")
                FirebaseDatabase.getInstance("https://hikeg-168f2-default-rtdb.asia-southeast1.firebasedatabase.app").reference
                    .child(item.author)
                    .child(item.dateTimeCreated)
                    .setValue(item)
                    .addOnSuccessListener {
                        removeFromLocalStorage(item)
                        record.remove(item)
                        Toast.makeText(holder.itemView.context,"Record Uploaded", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(holder.itemView.context,"Failed to Upload${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }else{
                Toast.makeText(holder.itemView.context,"No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun removeFromLocalStorage(item:AHike){
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(item.hikeName)
        editor.apply()
        record.remove(item)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.a_hike_record,parent,false)
        return ViewHolder(view,parent.context)
    }
    override fun getItemCount(): Int {
        return record.size
    }

}