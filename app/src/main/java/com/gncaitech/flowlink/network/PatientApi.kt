package com.gncaitech.flowlink.network

import com.google.gson.Gson
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class PatientDto(
    val id: String,
    val pid: String,
    val name: String,
    val age: Int?,
    val gender: String?,
    val surgeryData: String?,
    val program: String?,
    val scheduled: String?,
    val status: String?,
)

interface PatientApi {
    @GET("api/patients")
    suspend fun getPatients():Response<List<PatientDto>>
}

val patientApi: PatientApi = Retrofit.Builder()
    .baseUrl("http://flowlink.gncaitech.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(PatientApi::class.java)