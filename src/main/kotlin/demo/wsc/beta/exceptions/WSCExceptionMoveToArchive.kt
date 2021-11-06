package demo.wsc.beta.exceptions

import java.lang.Exception

open class WSCExceptionMoveToArchive: Exception() {
    override var message = "Move To Archive Is Not Running"
    override fun toString(): String {
        return "WSCExceptionInvalidUser{" +
                "message='" + message + '\'' +
                '}'
    }
}