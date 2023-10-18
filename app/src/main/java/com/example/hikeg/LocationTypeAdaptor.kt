package com.example.hikeg

import android.location.Location
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

class LocationTypeAdapter : TypeAdapter<Location>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Location) {
        out.beginObject()
        out.name("latitude").value(value.latitude)
        out.name("longitude").value(value.longitude)
        // Add more properties as needed
        out.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Location {
        val location = Location("custom")
        `in`.beginObject()
        while (`in`.hasNext()) {
            when (`in`.nextName()) {
                "latitude" -> location.latitude = `in`.nextDouble()
                "longitude" -> location.longitude = `in`.nextDouble()
            }
        }
        `in`.endObject()
        return location
    }
}
