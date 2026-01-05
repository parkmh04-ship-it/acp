package com.acp.merchant.adapter.outbound.persistence

import com.acp.merchant.application.port.output.TransactionPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Component
class TransactionAdapter(
    private val transactionManager: PlatformTransactionManager
) : TransactionPort {

    private val transactionTemplate = TransactionTemplate(transactionManager)

    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        // 이미 IO Dispatcher라면 바로 실행, 아니면 전환
        return withContext(Dispatchers.IO) {
            transactionTemplate.execute { status ->
                // TransactionTemplate 내부(Transaction Thread)에서 실행
                // runBlocking을 사용하여 suspend 함수(block)를 Blocking 방식으로 호출
                // 이렇게 해야 ThreadLocal 기반의 JDBC 트랜잭션이 유지됨
                runBlocking {
                    try {
                        block()
                    } catch (e: Exception) {
                        status.setRollbackOnly()
                        throw e
                    }
                }
            }!!
        }
    }
}
