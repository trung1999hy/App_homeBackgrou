package com.hst.beautifulwall.io

import com.hst.beautifulwall.io.model.Category
import com.hst.beautifulwall.io.model.Gif
import com.hst.beautifulwall.io.model.Image
import com.hst.beautifulwall.io.model.WallResponse
import retrofit2.Call
import retrofit2.http.GET

interface APIInterface {
    @GET("/wall/categories")
    fun getCategories() : Call<WallResponse<Category>>

    @GET("/wall/gifs")
    fun getGifs() : Call<WallResponse<Gif>>

    @GET("/wall/images")
    fun getImages() : Call<WallResponse<Image>>
}