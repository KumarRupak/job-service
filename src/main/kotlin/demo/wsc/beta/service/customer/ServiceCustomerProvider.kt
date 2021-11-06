/*;==========================================
; Title:  Customer Services Provider
; Author: Rupak Kumar
; Date:   17 Sep 2021
;==========================================*/
package demo.wsc.beta.service.customer


import org.springframework.beans.factory.annotation.Autowired
import demo.wsc.beta.repository.CustomerDetailsRepository
import demo.wsc.beta.repository.AuthCustomerRepository
import demo.wsc.beta.repository.CreditRepository
import demo.wsc.beta.service.mail.ServiceMailProvider
import demo.wsc.beta.repository.CustomerTransactionsRepository
import demo.wsc.beta.repository.WSCOwnerRepository
import kotlin.Throws
import javax.mail.MessagingException
import demo.wsc.beta.exceptions.WSCExceptionInvalidDetails
import demo.wsc.beta.model.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalDate
import demo.wsc.beta.model.transport.*
import org.springframework.stereotype.Service
import java.util.*

@Service
 class ServiceCustomerProvider : ServiceCustomer {

    @Autowired
    private lateinit var repoCusDetails: CustomerDetailsRepository

    @Autowired
    private lateinit var repoCusAuth: AuthCustomerRepository

    @Autowired
    private lateinit var repoCredit: CreditRepository

    @Autowired
    private lateinit var serviceMail: ServiceMailProvider

    @Autowired
    private lateinit var repoCusTrans: CustomerTransactionsRepository

    @Autowired
    private lateinit var repoAdmin: WSCOwnerRepository



    /**
     * Fetching the details of any particular card based on the card number
     *
     * @param 'Card Number'
     * @return - Credit Details
     */
    @Throws(WSCExceptionInvalidDetails::class)
    override fun getCardDetails(cardNumber: String): Credit {
        return if (repoCredit.findById(cardNumber).isPresent) {
            repoCredit.findById(cardNumber).get()
        } else {
            throw WSCExceptionInvalidDetails()
        }
    }


    /**
     * Paying of credit card bills
     *
     * @param 'PayEmi-DTO'
     * @return - RegistrationStatus-DTO
     */
    @Transactional
    @Throws(MessagingException::class)
    override fun payEmi(transfer: PayEmi): TransactionStatus {
        val returnInterst: Double
        if (transfer.mpin > 0 && transfer.installment > 0) {
            val transaction = TransactionStatus()
            if (repoCredit.findById(transfer.cardNumber).isPresent) {
                if (repoCusAuth.findById(repoCredit.findById(transfer.cardNumber).get().customerId)
                        .get().accountFlag == 1 && transfer.installment <= repoCredit.findById(transfer.cardNumber)
                        .get().cardPendingInstalment
                ) {
                    if (repoCusAuth.findById(repoCredit.findById(transfer.cardNumber).get().customerId).get()
                            .mpin == transfer.mpin
                    ) {
                        if (repoCusDetails.findById(repoCredit.findById(transfer.cardNumber).get().customerId)
                                .get().balance!! >=
                            repoCredit.findById(transfer.cardNumber).get().instalmentAmount * transfer.installment
                        ) {
                            if (repoCusAuth.findById(repoCredit.findById(transfer.cardNumber).get().customerId)
                                    .get().transactionLimit >= repoCredit.findById(transfer.cardNumber).get()
                                    .instalmentAmount * transfer.installment
                            ) {
                                val customer = repoCusDetails.findById(
                                    repoCredit.findById(transfer.cardNumber).get().customerId
                                ).get()
                                val card = repoCredit.findById(transfer.cardNumber).get()
                                val bank = repoAdmin.findById(card.branchId).get()
                                val amount=customer.balance
                                //Transaction begin
                                if(card.instalamentDate!!.compareTo(Calendar.getInstance().time)>0) {
                                    returnInterst= ((card.instalmentAmount) - (card.cardSpend!! / card.cardPendingInstalment))
                                    bank.returnInterest =bank.returnInterest+returnInterst
                                    customer.balance =customer.balance-
                                            (((card.cardSpend!!/card.cardPendingInstalment)*transfer.installment)+returnInterst.toLong())
                                }else
                                {
                                    customer.balance =
                                        customer.balance - (card.instalmentAmount * transfer.installment).toLong()
                                    returnInterst= ((card.instalmentAmount * transfer.installment) - ((card.cardSpend!! / card.cardPendingInstalment) * transfer.installment))
                                    bank.returnInterest =bank.returnInterest+returnInterst

                                }
                                card.cardLimit=(card.cardLimit!! + ((card.cardSpend!!/card.cardPendingInstalment)*transfer.installment))
                                card.cardSpend=(card.cardSpend!! - ((card.cardSpend!!/card.cardPendingInstalment)*transfer.installment))
                                card.cardPendingInstalment=(card.cardPendingInstalment - transfer.installment)
                                card.cardPaidInstalment=(card.cardPaidInstalment + transfer.installment)
                                val calendar = Calendar.getInstance()
                                calendar.time = card.instalamentDate
                                calendar.add(Calendar.MONTH, transfer.installment) //add Month
                                card.instalamentDate=(calendar.time)
                                card.instalamentDateShowUser=(calendar.time.toString())

                                if(card.cardPendingInstalment<1) {
                                    card.cardSpend=0
                                    card.instalmentAmount= 0.0
                                    card.cardPaidInstalment=0
                                    card.instalamentDateShowUser="NA"
                                    card.instalamentDate=Calendar.getInstance().time
                                }
                                repoCusDetails.save(customer)
                                repoCredit.save(card)
                                repoAdmin.save(bank)
                                //Transaction end

                                //Transaction
                                val transactions = CustomerTransactions()
                                transactions.transactionId=(String.format("%05d", Random().nextInt(100000)))
                                transactions.panId=(customer.panId)
                                transactions.senderAccount=(customer.accountNumber.toString())
                                transactions.receiverAccount=(bank.accountNo.toString())
                                transactions.senderName=(customer.name)
                                transactions.transactionDate=(LocalDateTime.now())
                                transactions.transactionDateShowUser=(LocalDate.now().toString())
                                transactions.amount=(amount-customer.balance)
                                transactions.interest=(returnInterst.toString())
                                transactions.transactionDetails=("Paid for EMI to " + card.cardNumber)
                                repoCusTrans.save(transactions)
                                transaction.transactionAmount = amount-customer.balance
                                transaction.setTransactionReason(1)
                                transaction.setTransactionStatus(1)
                                serviceMail.sendCredit(
                                    customer.email,
                                    (card.cardSpend!! - card.instalmentAmount * transfer.installment).toLong(),
                                    card.cardNumber
                                )

                            } else {
                                transaction.transactionAmount = (repoCredit.findById(transfer.cardNumber).get()
                                    .instalmentAmount * transfer.installment).toLong()
                                transaction.setTransactionReason(4)
                                transaction.setTransactionStatus(0)
                            }
                        } else {
                            transaction.transactionAmount = (repoCredit.findById(transfer.cardNumber).get()
                                .instalmentAmount * transfer.installment).toLong()
                            transaction.setTransactionReason(3)
                            transaction.setTransactionStatus(0)
                        }
                    } else {
                        transaction.transactionAmount = (repoCredit.findById(transfer.cardNumber).get()
                            .instalmentAmount * transfer.installment).toLong()
                        transaction.setTransactionReason(2)
                        transaction.setTransactionStatus(0)
                    }
                } else {
                    transaction.transactionAmount = (repoCredit.findById(transfer.cardNumber).get()
                        .instalmentAmount * transfer.installment).toLong()
                    transaction.setTransactionReason(0)
                    transaction.setTransactionStatus(0)
                }
            } else {
                transaction.transactionAmount = (repoCredit.findById(transfer.cardNumber).get()
                    .instalmentAmount * transfer.installment).toLong()
                transaction.setTransactionReason(0)
                transaction.setTransactionStatus(0)
            }
            return transaction
        }
        else{
            return TransactionStatus()
        }

    }


}