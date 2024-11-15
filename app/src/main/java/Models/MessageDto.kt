package Models

data class MessageDto(
    val token:String?,  // token with which i will access the friend
    val title: String, // name of the user who send the message
    val body: String ,// body of the message
    val image: String, // image of the user who sent the message

)
