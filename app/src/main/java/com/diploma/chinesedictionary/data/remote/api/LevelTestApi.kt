package com.diploma.chinesedictionary.data.remote.api

import com.diploma.chinesedictionary.data.remote.dto.LevelTestResultRequestDto
import com.diploma.chinesedictionary.data.remote.dto.LevelTestResultResponseDto
import com.diploma.chinesedictionary.data.remote.dto.TestQuestionBatchDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LevelTestApi {

    @GET("api/leveltest/questions")
    suspend fun getQuestions(
        @Query("hsk_level") hskLevel: Int,
        @Query("count") count: Int = 10,
    ): TestQuestionBatchDto

    @POST("api/leveltest/result")
    suspend fun saveResult(@Body body: LevelTestResultRequestDto): LevelTestResultResponseDto
}
