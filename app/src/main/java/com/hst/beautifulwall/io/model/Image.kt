package com.hst.beautifulwall.io.model

import com.google.gson.annotations.SerializedName
import com.hst.beautifulwall.models.content.Posts

class Image : Wall {

    @SerializedName("type")
    private var type : String? = null
    @SerializedName("imageUrl")
    private var imageUrl : String? = null
    @SerializedName("title")
    private var title : String? = null
    @SerializedName("category")
    private var category : String? = null
    @SerializedName("isFeature")
    private var isFeature : Int = 0
    @SerializedName("isNeedPoint")
    private var isNeedPoint : Int = 0

    override fun getType(): String? = type

    override fun getTitle(): String? = title

    override fun getImageUrl(): String? = imageUrl

    fun getCategory(): String? = category
    fun isFeature(): Int = isFeature
    fun isNeedPoint() : Int = isNeedPoint

    fun toPost(): Posts = Posts(
        this.title ?: "", this.category ?: "", if (this.isFeature == 1) "yes" else "no", this.imageUrl,
        this.isNeedPoint == 1
    )
}