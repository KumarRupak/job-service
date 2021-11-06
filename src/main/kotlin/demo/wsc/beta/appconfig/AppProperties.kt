package demo.wsc.beta.appconfig

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


@Component
class AppProperties {

    @Value("\${job.autopay}")
    lateinit var jobAutoPay:String

    @Value("\${job.movetoarchive}")
    lateinit var jobMoveToArchive:String
}