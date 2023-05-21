package com.hst.beautifulwall.io.model

import com.google.gson.annotations.SerializedName
import com.hst.beautifulwall.models.content.Posts

class Gif : Wall {

    @SerializedName("type")
    private var type : String? = null
    @SerializedName("imageUrl")
    private var imageUrl : String? = null
    @SerializedName("title")
    private var title : String? = null

    override fun getType(): String? = type

    override fun getTitle(): String?  = title

    override fun getImageUrl(): String?  = imageUrl

    fun toPost(): Posts = Posts(
        this.title ?: "", "", "no", this.imageUrl,
        false
    )

}