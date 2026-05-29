package com.gncaitech.flowlink.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val email: String, val institutionId: String?)
data class ErrorResponse(val error: String)

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>
}

val authApi: AuthApi = Retrofit.Builder()
    .baseUrl("http://flowlink.gncaitech.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(AuthApi::class.java)
