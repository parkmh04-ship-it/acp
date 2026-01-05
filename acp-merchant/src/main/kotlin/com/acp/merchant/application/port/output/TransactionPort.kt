package com.acp.merchant.application.port.output

/**
 * 명시적 트랜잭션 제어를 위한 포트.
 * Application Service 계층에서 트랜잭션 경계를 설정할 때 사용합니다.
 */
interface TransactionPort {
    /**
     * 주어진 블록을 하나의 트랜잭션으로 실행합니다.
     * 블록 내에서 예외가 발생하면 트랜잭션은 롤백됩니다.
     */
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
