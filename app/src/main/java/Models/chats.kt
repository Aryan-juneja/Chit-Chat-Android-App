package Models

import java.util.Date

data class chats(
    var mssg:String,
    var from:String,
    var name:String,
    var image:String,
    var time:Date = Date(),
    var count:Int
){
    constructor():this("","","","",Date(),0)
}
