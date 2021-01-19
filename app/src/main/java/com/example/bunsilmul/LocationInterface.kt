package com.example.bunsilmul

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

public interface LocationInterface{
    @POST("api/locations")
    fun CreateLocation(
            @Body locations: locations
    ): Call<wantmessage>

    @GET("api/locations/{uid}")
    fun GetLocation(
            @Path("uid") uid: String?
    ): Call<Array<user_location>>


}