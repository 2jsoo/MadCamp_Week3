package com.example.bunsilmul

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.kakao.auth.IApplicationConfig
import com.kakao.auth.KakaoAdapter
import com.kakao.auth.KakaoSDK


/**
 * @author kevin.kang
 * Created by kevin.kang on 2017. 3. 2..
 */
class KakaoLoginApplication() : Application() {
    private lateinit var self : KakaoLoginApplication
    override fun onCreate() {
        super.onCreate()
        self = this
        FirebaseApp.initializeApp(this)
        KakaoSDK.init(object : KakaoAdapter() {
            override fun getApplicationConfig(): IApplicationConfig {
                return IApplicationConfig { self }
            }
        })

    }
}