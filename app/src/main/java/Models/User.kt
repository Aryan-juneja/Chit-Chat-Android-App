package Models

data class User(
    val name:String,
    val imageUrl:String,
    val status:String,
    val phoneNumber:String,
    val uid:String,
    val deviceToken:String,
    val onlineStatus:String
) {
    constructor():this("","","","","","","")
    constructor(imageUrl: String,name: String,phoneNumber: String,uid: String,status: String? = null):this(
        name,
        imageUrl,
        status=status ?:"Hey There I am Using Chit-chat",
        phoneNumber,
        uid,
        "",
        System.currentTimeMillis().toString()
    )
}