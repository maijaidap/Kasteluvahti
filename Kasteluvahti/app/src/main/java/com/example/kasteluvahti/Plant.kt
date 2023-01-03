package com.example.kasteluvahti

class Plant (
    var plantId: String = "", val plantName: String = "", val wateringFrequency: Int = 0,
    var lastTimeWatered: String = "", val plantImage: String = "") {

    override fun toString(): String {
        return "$plantName"
    }
}