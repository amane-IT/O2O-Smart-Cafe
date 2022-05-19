package com.ssafy.smartstore.dto

import java.io.Serializable

data class Grade(
    val img: String,
    val title: String
): Serializable{

    var step: Int = 0
    var to: Int = 0

    constructor(img: String, step: Int, to: Int, title: String): this(img, title){
        this.step = step
        this.to = to
    }
}
