/*;==========================================
; Title:  Automatic job Of WSCServices
; Author: Rupak Kumar
; Date:   26 Sep 2021
;==========================================*/



package demo.wsc.beta.service.jobs


import demo.wsc.beta.algorithms.PasswordEncode.Encoder
import demo.wsc.beta.appconfig.AppProperties
import demo.wsc.beta.model.transport.PayEmi
import demo.wsc.beta.repository.CustomerTransactionsArchiveRepository
import demo.wsc.beta.repository.CustomerTransactionsRepository
import demo.wsc.beta.repository.PayAutoEmiRepository
import demo.wsc.beta.service.customer.ServiceCustomerProvider
import demo.wsc.beta.service.mail.ServiceMailProvider
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
@Slf4j
open class ServiceWscJobsProvider:ServiceWscJobs {

    @Autowired
    lateinit var repoPayAuto : PayAutoEmiRepository

    @Autowired
    lateinit var serviceCus : ServiceCustomerProvider

    @Autowired
    lateinit var repoCustomerTransactions : CustomerTransactionsRepository

    @Autowired
    lateinit var repoCustomerTransactionsArchive : CustomerTransactionsArchiveRepository

    @Autowired
    lateinit var appProperties: AppProperties


    val log = LoggerFactory.getLogger(ServiceMailProvider::class.java)

    /**
     * Cron job for paying auto emil bills
     *
     * @param 'card details'
     * @return - boolen
     */
    @Scheduled(cron = "0 0 11 * * *")
    override fun setAutoPay() {
        println("hii Rupak")
        log.info("AutoPay job is running")
        if (!repoPayAuto.findAll().isEmpty()) {

            repoPayAuto.findAll().stream().forEach {
                if (it.serviceFlag == 1 &&
                    Calendar.getInstance().time.compareTo(serviceCus.getCardDetails(it.cardNumber!!).instalamentDate)>=0) {
                    var emi = PayEmi(it.cardNumber!!, 1, it.mPin)
                    serviceCus.payEmi(emi)
                    log.info("EMI successfully paid for ${it.cardNumber}")
                }
            }
            Thread.sleep(20000)
        }
    }



    /**
     * Cron job for move to archive
     * At 6 p.m on the last day of every month
     */
    @Scheduled(fixedDelay = 10000)
    override fun moveToArchive() {
        val transactions = repoCustomerTransactions.findAll()

        if(transactions.isNotEmpty())
        {
            transactions.stream().forEach { element ->
                run {
                    if (LocalDateTime.now().compareTo(element.transactionDate) > appProperties.jobMoveToArchive.toInt()) {
                        val tran=repoCustomerTransactions.findById(Encoder.encode(element.transactionId)).get()
                        println(tran)
                    }

                }
            }
        }
    }
}


