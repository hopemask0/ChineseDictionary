package com.diploma.chinesedictionary.data.remote.api

import com.diploma.chinesedictionary.data.remote.dto.AddWordRequestDto
import com.diploma.chinesedictionary.data.remote.dto.CreateListRequestDto
import com.diploma.chinesedictionary.data.remote.dto.SubscribeRequestDto
import com.diploma.chinesedictionary.data.remote.dto.UpdateListRequestDto
import com.diploma.chinesedictionary.data.remote.dto.UserListDetailDto
import com.diploma.chinesedictionary.data.remote.dto.UserListDto
import com.diploma.chinesedictionary.data.remote.dto.WordListTemplateDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit-интерфейс для эндпоинтов списков слов.
 *
 * Все запросы автоматически получают заголовок X-User-Id через
 * AuthInterceptor (поэтому в методах его явно не указываем).
 */
interface ListsApi {

    @GET("api/lists/catalog")
    suspend fun getCatalog(): List<WordListTemplateDto>

    @GET("api/lists/my")
    suspend fun getMyLists(): List<UserListDto>

    @POST("api/lists/my")
    suspend fun createList(@Body body: CreateListRequestDto): UserListDto

    @POST("api/lists/subscribe")
    suspend fun subscribeToTemplate(@Body body: SubscribeRequestDto): UserListDto

    @GET("api/lists/my/{listId}")
    suspend fun getListDetail(@Path("listId") listId: Int): UserListDetailDto

    @PATCH("api/lists/my/{listId}")
    suspend fun updateList(
        @Path("listId") listId: Int,
        @Body body: UpdateListRequestDto,
    ): UserListDto

    @DELETE("api/lists/my/{listId}")
    suspend fun deleteList(@Path("listId") listId: Int)

    @POST("api/lists/my/{listId}/items")
    suspend fun addWord(
        @Path("listId") listId: Int,
        @Body body: AddWordRequestDto,
    )

    @DELETE("api/lists/my/{listId}/items/{wordId}")
    suspend fun removeWord(
        @Path("listId") listId: Int,
        @Path("wordId") wordId: Int,
    )
}
