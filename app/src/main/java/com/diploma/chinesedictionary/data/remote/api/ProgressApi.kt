package com.diploma.chinesedictionary.data.remote.api

import com.diploma.chinesedictionary.data.remote.dto.ProgressItemDto
import com.diploma.chinesedictionary.data.remote.dto.ProgressStatsDto
import com.diploma.chinesedictionary.data.remote.dto.ReviewAnswerDto
import com.diploma.chinesedictionary.data.remote.dto.TrainerSessionDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProgressApi {

    /** Подготовить набор карточек для тренировки по списку. */
    @GET("api/progress/session/{listId}")
    suspend fun getSession(@Path("listId") listId: Int): TrainerSessionDto

    /** Записать ответ пользователя на одно слово. */
    @POST("api/progress/review")
    suspend fun review(@Body body: ReviewAnswerDto): ProgressItemDto

    /** Получить общую статистику. */
    @GET("api/progress/stats")
    suspend fun getStats(): ProgressStatsDto
}
