package com.example.kasteluvahti

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.ArrayList

// Firebase variables
var firestore: FirebaseFirestore? = null
var plantReference: CollectionReference? = null
var plantListAdapter: ArrayAdapter<*>? = null

class MainActivity : AppCompatActivity() {
    private val TAG = "kasteluvahti-MainAct"

    private var name: EditText? = null
    private var wateringFrequency: EditText? = null
    private var allPlants: MutableList<Plant?>? = null
    private var plantsListView: ListView? = null
    private var context: Context? = null

    // Receiver for a new intent
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {  result: ActivityResult ->
            Log.e(TAG, "getResult")
            if (result.resultCode == RESULT_OK) {
                val value = result.data?.getStringExtra("newPlant")
                if(value != null)
                    Log.e(TAG, value)

                val itemList = value?.split(";")
                val name = itemList?.get(0).toString()
                val wateringFreq = itemList?.get(1).toString().toInt()
                val lastTimeWatered = itemList?.get(2).toString()
                val plantImage = itemList?.get(3).toString()
                val newPlant = Plant("", name, wateringFreq, lastTimeWatered, plantImage)

                // Adds a new plant into the database
                plantReference = firestore!!.collection("kasteluvahti")
                plantReference!!.add(newPlant).addOnSuccessListener { ref ->
                    Log.d(TAG, "DocumentSnapshot written with ID: ${ref.id}")
                    newPlant.plantId = ref.id
                }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }
                (allPlants as ArrayList<Plant?>).add(newPlant)

                // Updates the list view
                plantListAdapter = PlantArrayAdapter(context, R.layout.item_plant, allPlants)
                (plantListAdapter as PlantArrayAdapter).notifyDataSetChanged()
                plantsListView!!.adapter = plantListAdapter
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this

        name = findViewById(R.id.editTextTextPlantName)
        wateringFrequency = findViewById(R.id.editTextWateringFrequency)
        plantsListView = findViewById(R.id.plantListView)
        allPlants = java.util.ArrayList()

        firestore = FirebaseFirestore.getInstance()
        plantReference = firestore!!.collection("kasteluvahti")
        context = this

        plantListAdapter = PlantArrayAdapter(context, R.layout.item_plant, allPlants)
        (plantListAdapter as PlantArrayAdapter).notifyDataSetChanged()
        plantsListView!!.adapter = plantListAdapter

        updateView()

        // Functionalities for "add plant" -floating action button.
        val addNewPlantButton: FloatingActionButton = findViewById<View>(R.id.addNewPlantFloatingButton) as FloatingActionButton
        addNewPlantButton.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(context, NewPlantActivity::class.java)
            getResult.launch(intent)
        })
    }

    /**
     * Fetches the needed data from the database and updates the application's view
     */
    private fun updateView() {
        firestore!!.collection("kasteluvahti")
            .addSnapshotListener { queryDocumentSnapshots, e ->
                allPlants!!.clear()
                if (queryDocumentSnapshots != null) {
                    for (snapshot in queryDocumentSnapshots) {
                        val plant = snapshot.toObject(Plant::class.java)
                        // Saves the document id as plant's id
                        plant.plantId = snapshot.id
                        allPlants!!.add(plant)
                    }
                    plantListAdapter = PlantArrayAdapter(context, R.layout.item_plant, allPlants)
                    (plantListAdapter as PlantArrayAdapter).notifyDataSetChanged()
                    plantsListView!!.adapter = plantListAdapter
                } else {
                    Log.e(TAG, "Querysnapshot = null")
                }
            }
    }
}