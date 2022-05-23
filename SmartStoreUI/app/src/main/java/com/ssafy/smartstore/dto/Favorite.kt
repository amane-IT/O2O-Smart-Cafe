package com.ssafy.smartstore.dto

import java.io.Serializable

data class Favorite(
    val productId: Int,
    val userId: String
): Serializable{
    var id: Int = 0

    constructor(id: Int, productId: Int, userId: String): this(productId, userId){
        this.id = id
    }
}