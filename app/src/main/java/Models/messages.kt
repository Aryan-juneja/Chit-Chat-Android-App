package Models

import android.content.Context
import utils.formatAsHeader
import java.util.Date
interface chatEvent{
    val sentAt:Date
}
data class messages(
    val mssg:String,
    val senderId:String,
    val msgId:String,
    val type:String="Text",
    val status:Int=1,
    var liked:Boolean=false,
    override val sentAt: Date=Date()
):chatEvent{
    constructor():this("","","","",1,false,Date())
}

data class DateHeader(override val sentAt: Date=Date(),val context:Context):chatEvent{
    val date :String =sentAt.formatAsHeader(context)
}