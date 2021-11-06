/*;==========================================
; Title:  Customer Services
; Author: Rupak Kumar
; Date:   16 Sep 2021
;==========================================*/
package demo.wsc.beta.service.customer

import kotlin.Throws
import demo.wsc.beta.exceptions.WSCExceptionInvalidModeldata
import demo.wsc.beta.exceptions.WSCExceptionInvalidUser
import javax.mail.MessagingException
import demo.wsc.beta.model.Credit
import demo.wsc.beta.exceptions.WSCExceptionInvalidDetails
import demo.wsc.beta.model.CustomerDetails
import demo.wsc.beta.model.CustomerTransactions
import demo.wsc.beta.model.WSCCards
import demo.wsc.beta.model.transport.*
import java.text.ParseException

 interface ServiceCustomer {


    @Throws(WSCExceptionInvalidDetails::class, MessagingException::class, ParseException::class)
    fun payEmi(transfer: PayEmi): TransactionStatus

    @Throws(WSCExceptionInvalidDetails::class)
    fun getCardDetails(cardNumber: String): Credit


}