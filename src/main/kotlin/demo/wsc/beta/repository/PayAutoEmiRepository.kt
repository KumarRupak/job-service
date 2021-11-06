/*; Title:  Entity  for Setup Auto Pay Job Details
; Author: Rupak Kumar
; Date:   4 Oct 2021
;==========================================*/

package demo.wsc.beta.repository

import demo.wsc.beta.model.transport.PayAutoEmi
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PayAutoEmiRepository : MongoRepository<PayAutoEmi, String> {
}