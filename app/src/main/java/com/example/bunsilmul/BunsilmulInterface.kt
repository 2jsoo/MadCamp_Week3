package com.example.bunsilmul

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

public interface BunsilmulInterface{
    @POST("api/bunsilmuls")
    fun CreateBunsilmul(
        @Body bunsilmul: bunsilmul
    ): Call<message>

    @GET("api/bunsilmuls/all")
    fun GetBunsilmul(
    ): Call<Array<bunsilmul>>

    @GET("/api/bunsilmuls/photo/{id}")
    fun GetBunsilmulPhoto(
        @Path("id") id: String
    ): Call<bunsilmulphoto>
}