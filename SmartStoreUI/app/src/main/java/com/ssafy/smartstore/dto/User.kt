package com.ssafy.smartstore.dto

import java.io.Serializable

data class User (var id: String, var name: String, var stamps: Int = 0): Serializable {
    var pass: String = ""
    var stampList: List<Stamp> = emptyList()

    constructor(id: String, name: String, pass: String): this(id, name){
        this.pass = pass
    }

    constructor(id: String, name: String, pass: String, stampList: List<Stamp> = emptyList(), stamps: Int): this(id, name, stamps){
        this.pass = pass
        this.stampList = stampList
    }
}
