package com.example.kasteluvahti

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.util.*
import java.util.concurrent.TimeUnit


class PlantArrayAdapter
    (context: Context?, resource: Int, objects: List<Plant?>?) :
    ArrayAdapter<Plant?>(context!!, resource, objects!!) {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (convertView == null) {
            convertView =
                (context as Activity).layoutInflater.inflate(R.layout.item_plant, parent, false)
        }

        val plantName = convertView!!.findViewById<View>(R.id.nameTextView) as TextView
        val daysLeftTilWatering = convertView!!.findViewById<View>(R.id.daysTilWateringTextView) as TextView
        val img: ImageView = convertView!!.findViewById(R.id.plantImageViewArrayAdapter)
        val plant = getItem(position)

        // Set the plant's picture
        val plantImageUri = plant!!.plantImage
        val uri: Uri = Uri.parse(plantImageUri)
        Log.e("", "plantImageUri: " +plantImageUri)
        val contentResolver: ContentResolver = context.contentResolver
        if(!plantImageUri.isEmpty()) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source)
            img.setImageBitmap(bitmap)
        }

        // Add plant name to the main view's listing
        plantName.visibility = View.VISIBLE
        if (plant != null) {
            plantName.text = "" + plant.plantName
        }

        // Adds plant's days until watering to the main view's listing
        daysLeftTilWatering.visibility = View.VISIBLE
        if (plant != null) {
            try {
                val ltw = Date(plant?.lastTimeWatered.toLong())
                val displayTextStr = countDaysUntilWatering(ltw, plant?.wateringFrequency).toString() + " days left"
                daysLeftTilWatering.text = displayTextStr
            } catch (e : IllegalArgumentException){
            }
        }

        val deleteButton = convertView!!.findViewById<View>(R.id.deletePlantImageButton)
        // Saves the plant object into the delete button's tag
        deleteButton.tag = plant
        deleteButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                // Get the correct item by saving it into a temp variable
                val tempPlant : Plant? = v.getTag() as Plant?;

                Log.e("", "plantid: " + tempPlant!!.plantId)
                // Delete plant from the database
                firestore!!.collection("kasteluvahti").document(tempPlant!!.plantId).delete();
                Toast.makeText(context, "Plant deleted: " + tempPlant?.plantName , Toast.LENGTH_SHORT).show()
            }
        })

        val waterPlantButton = convertView!!.findViewById<View>(R.id.waterPlantButton)
        // Saves the plant object into the delete button's tag
        waterPlantButton.tag = plant
        waterPlantButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                // Get the correct item by saving it into a temp variable
                val tempPlant : Plant? = v.getTag() as Plant?;

                // Update the last time watered date
                val currentDate = Calendar.getInstance().time;
                tempPlant!!.lastTimeWatered = currentDate.time.toString()
                val ltw = Date(tempPlant?.lastTimeWatered.toLong())
                val daysLeft = countDaysUntilWatering(ltw, tempPlant.wateringFrequency)
                daysLeftTilWatering.text = "$daysLeft days left"
                // Update the database
                firestore!!.collection("kasteluvahti").document(tempPlant!!.plantId).set(tempPlant);
                Toast.makeText(context, "Plant " + tempPlant.plantName +" watered!" , Toast.LENGTH_SHORT).show()
            }
        })
        return convertView
    }

    /**
     * Counts the amount of days left until plant needs to be watered.
     */
    private fun countDaysUntilWatering(lastTimeWatered: Date?, wateringFrequency: Int?): Int {
        if(wateringFrequency == null)
            return 0

        var currentDate = Calendar.getInstance().time;
        val diffInMillies = currentDate.time - lastTimeWatered!!.time;
        val difference = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        val daysUntilWatering = wateringFrequency - difference

        // If the value is negative show days left until watering as 0
        if (daysUntilWatering < 0){
            return 0
        }
        return daysUntilWatering.toInt()
    }
}