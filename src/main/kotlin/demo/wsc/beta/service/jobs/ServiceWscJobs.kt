/*;==========================================
; Title:  Automatic job Of WSCServices
; Author: Rupak Kumar
; Date:   26 Sep 2021
;==========================================*/


package demo.wsc.beta.service.jobs

import demo.wsc.beta.exceptions.WSCExceptionInvalidUser
import demo.wsc.beta.exceptions.WSCExceptionMoveToArchive
import demo.wsc.beta.model.transport.PayAutoEmi


 interface ServiceWscJobs  {

    @Throws(WSCExceptionInvalidUser::class)
    fun setAutoPay()

     @Throws(WSCExceptionMoveToArchive::class)
     fun moveToArchive()
}
