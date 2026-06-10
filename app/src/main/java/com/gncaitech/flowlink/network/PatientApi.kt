package com.gncaitech.flowlink.network

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query

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

data class SessionRequest(
    val id: String,
    val patientId: String,
    val date: String,
    val kind: String, // grip | dumbbell | wrist_rotation
    val repsCompleted: Int,
    val repsTarget: Int,
    val totalSets: Int,
    val setSeconds: Int,
    val postureScore: Int, // 0 (자세 피드백 미구현)
    val durationSec: Int,
    val feedback: String, //perfect minor major
    val landmarks: String = "",
)

data class SessionDto(
    val id: String,
    val patientId: String,
    val date: String,
    val kind: String,
    val repsCompleted: Int,
    val repsTarget: Int,
    val postureScore: Int,
    val durationSec: Int,
    val feedback: String,
)

// 세션 목록 응답 (페이지네이션)
data class SessionPageDto(
    val data: List<SessionDto>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
)

interface PatientApi {
    @GET("api/patients")
    suspend fun getPatients(): Response<List<PatientDto>>

    @POST("api/patients")
    suspend fun registerPatient(@Body body: RegisterPatientRequest): Response<Unit>

    @POST("api/sessions")
    suspend fun saveSession(@Body body: SessionRequest): Response<Unit>

    @GET("api/sessions")
    suspend fun getSessions(
        @Query("patientId") patientId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): Response<SessionPageDto>

    @DELETE("api/sessions/{id}")
    suspend fun deleteSession(@Path("id") id: String): Response<Unit>
}

// 앱 전역 토큰 저장소
object AuthTokenHolder {
    var token: String? = null
}

// Bearer 토큰 자동 첨부 인터셉터
private val authInterceptor = Interceptor { chain ->
    val token = AuthTokenHolder.token
    val request = if (token != null) {
        chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
    } else {
        chain.request()
    }
    chain.proceed(request)
}

val patientApi: PatientApi = Retrofit.Builder()
    .baseUrl("http://flowlink.gncaitech.com/")
    .client(OkHttpClient.Builder().addInterceptor(authInterceptor).build())
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(PatientApi::class.java)
