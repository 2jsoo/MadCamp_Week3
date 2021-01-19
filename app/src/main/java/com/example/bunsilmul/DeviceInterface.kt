package com.example.bunsilmul

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

public interface DeviceInterface{
    @POST("api/device")
    fun CreateDevice(
        @Body device: device
    ): Call<devicemessage>
}

data class device(
    var uid: String,
    var devicetoken: String
)

data class devicemessage(
    var message: String
)