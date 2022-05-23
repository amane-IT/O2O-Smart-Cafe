package com.ssafy.smartstore.dto

import java.io.Serializable

data class Comment(
    val productId: Int,
    val userId: String,
    var rating: Double,
    var comment: String,
): Serializable{

    var id: Int = 0

    constructor(id: Int, productId: Int, userId: String, rating: Double, comment: String): this(productId, userId, rating, comment){
        this.id = id
    }
}