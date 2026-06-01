package com.gncaitech.flowlink.network

import com.google.gson.Gson
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

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

data class RegisterPatientRequest(
    val id: String,
    val pid: String,
    val name: String,
    val age: Int,
    val gender: String,
    val surgeryDate: String,
    val surgeryLocation: String,
    val anastomosis: String,
    val surgeonName: String,
    val baselineDiameterMm: Double,
    val baselineFlowMlMin: Int,
    val previousAvfHistory: String,
    val maturity: Int,
    val program: String,
    val adherence: Int,
    val status: String,
    val createdAt: String,
)

interface PatientApi {
    @GET("api/patients")
    suspend fun getPatients():Response<List<PatientDto>>

    @POST("api/patients")
    suspend fun registerPatient(@Body body: RegisterPatientRequest): Response<Unit>
}

val patientApi: PatientApi = Retrofit.Builder()
    .baseUrl("http://flowlink.gncaitech.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(PatientApi::class.java)