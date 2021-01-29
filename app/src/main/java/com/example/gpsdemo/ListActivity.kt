package com.example.gpsdemo

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.gpsdemo.databinding.ActivityListBinding

class ListActivity : AppCompatActivity() {
    private lateinit var binding : ActivityListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val savedList = (this.application as MyApplication).myLocations.map { it.toString() }
        val arrayAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            savedList
        )
        binding.lvWaypoints.adapter = arrayAdapter
    }
}