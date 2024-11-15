package service

import Models.MessageDto
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

interface MessageApiService {

    @POST("sendNotification")
    suspend fun sendNotification(@Body body: MessageDto): ResponseBody
}
