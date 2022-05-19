package com.ssafy.smartstore.dto

import java.io.Serializable

data class User (var id: String, var name: String, var pass: String, var stampList: List<Stamp> = emptyList(), var stamps: Int = 0): Serializable {
}
