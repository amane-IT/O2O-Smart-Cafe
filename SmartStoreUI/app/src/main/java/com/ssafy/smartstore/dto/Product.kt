package com.ssafy.smartstore.dto

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

data class Product(var name: String, var type: String, var price: Int, var img: String): Serializable{

    var id: Int = 0

    constructor(id: Int, name: String, type: String, price: Int, img: String): this(name, type, price, img){
        this.id = id
    }
}
