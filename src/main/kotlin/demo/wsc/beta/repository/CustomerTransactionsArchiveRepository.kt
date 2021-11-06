package demo.wsc.beta.repository

import demo.wsc.beta.model.CustomerTransactions
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CustomerTransactionsArchiveRepository : MongoRepository<CustomerTransactions, String> {
    @Query("{'panId':'?0'}")
    fun findAllTransactions(panId: String?): List<CustomerTransactions>
}