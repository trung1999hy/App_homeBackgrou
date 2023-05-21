package com.hst.beautifulwall.io.model

import com.google.gson.annotations.SerializedName

data class WallResponse<T>(
    @SerializedName("code")
    var code : Int = 401,
    @SerializedName("data")
    var data : List<T>,
    @SerializedName("success")
    var success : Boolean = false
)
