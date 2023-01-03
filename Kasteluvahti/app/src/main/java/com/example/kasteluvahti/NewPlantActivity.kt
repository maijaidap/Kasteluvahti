package com.example.kasteluvahti

import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*


class NewPlantActivity : AppCompatActivity() {
    private val TAG = "newPlant"
    private var context: Context? = null

    private var name: EditText? = null
    private var wateringFrequency: EditText? = null
    private var calendar: CalendarView? = null
    private var lastTimeWateredDate: Date? = null
    private var plantImageUri: String? = ""

    // Receiver for a the camera intent
    @RequiresApi(Build.VERSION_CODES.P)
    private val getResultFromCamera =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {  result: ActivityResult ->
            Log.e(TAG, "getResult")
            if (result.resultCode == RESULT_OK) {
                val value = result.data?.getStringExtra("plantImageUri")
                plantImageUri = value
                if(value != null)
                    Log.e(TAG, value)

                val img: ImageView = findViewById<View>(R.id.plantImageViewNewPlant) as ImageView
                val uri: Uri = Uri.parse(plantImageUri)
                val source = ImageDecoder.createSource(contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                img.setImageBitmap(bitmap)
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_plant)
        context = this
        name = findViewById(R.id.editTextTextPlantName)
        wateringFrequency = findViewById(R.id.editTextWateringFrequency)
        calendar = findViewById(R.id.calendarView);


        val takePictureButton: Button = findViewById(R.id.takePictureButton)
        takePictureButton.setOnClickListener {
                val intent = Intent(context, CameraActivity::class.java)
                getResultFromCamera.launch(intent)

        }

        val savePlantButton: Button = findViewById(R.id.addPlantButton)
        savePlantButton.setOnClickListener {
            val replyIntent = Intent()
            Log.e(TAG, "savePlant")
            if (TextUtils.isEmpty(name!!.text) || TextUtils.isEmpty(wateringFrequency!!.text)) {
                Toast.makeText(context,"Plant not added! Fill out all details.", Toast.LENGTH_SHORT).show()
            } else {
                addNewPlant()
                finish()
            }
        }

        calendar!!.setOnDateChangeListener(CalendarView.OnDateChangeListener { view, year, month, dayOfMonth ->
            val gregoCalendar = GregorianCalendar(year, month, dayOfMonth)
            lastTimeWateredDate = Date(gregoCalendar.timeInMillis)
        })
    }

    /**
     * Adds a new plant into the database.
     */
    private fun addNewPlant() {
        // Parameters from the EditText elements
        val pName = name!!.text.toString()
        val pWateringFreq = wateringFrequency!!.text.toString().toInt()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val lastTimeWateredStr = lastTimeWateredDate!!.time.toString()
        Log.e("", "lastTimeWateredStr: " + lastTimeWateredStr)

        // New plant as a string variable
        val newPlant = "$pName;$pWateringFreq;$lastTimeWateredStr;$plantImageUri"

        // Send new plant string variable to MainActivity
        val resultIntent = Intent();
        resultIntent.putExtra("newPlant", newPlant);
        setResult(RESULT_OK, resultIntent);
    }
}



