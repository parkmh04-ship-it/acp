package com.acp.merchant.support

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 수동 Docker 제어를 통한 통합 테스트 베이스 클래스.
 * Testcontainers 라이브러리 의존성 없이 ProcessBuilder로 docker cli를 직접 호출합니다.
 */
abstract class IntegrationTestBase {

    companion object {
        private var redisContainerId: String? = null
        private var redisPort: Int = 6379

        init {
            setupRedis()
            // JVM 종료 시 컨테이너 정리
            Runtime.getRuntime().addShutdownHook(Thread {
                stopRedis()
            })
        }

        private fun setupRedis() {
            try {
                println("Starting Redis container using manual docker cli...")
                // 1. Redis Docker 컨테이너 실행
                val process = ProcessBuilder("docker", "run", "-d", "-P", "redis:7-alpine")
                    .redirectErrorStream(true)
                    .start()

                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                var containerId: String? = null
                
                while (reader.readLine().also { line = it } != null) {
                    println("Docker output: $line")
                    // 컨테이너 ID는 64자 이상 16진수 (혹은 짧은 ID)
                    if (line != null && line!!.matches(Regex("^[a-f0-9]{64}$"))) {
                        containerId = line
                    }
                }
                
                process.waitFor()
                redisContainerId = containerId?.trim()

                if (redisContainerId != null && redisContainerId!!.isNotEmpty()) {
                    println("Redis container started: $redisContainerId")
                    
                    // 3. 매핑된 호스트 포트 조회
                    val portProcess = ProcessBuilder("docker", "port", redisContainerId!!, "6379")
                        .start()
                    
                    val portReader = BufferedReader(InputStreamReader(portProcess.inputStream))
                    val portOutput = portReader.readLine()?.trim()
                    
                    // 4. 포트 파싱
                    redisPort = portOutput?.substringAfterLast(":")?.toInt() ?: 6379
                    println("Redis mapped port: $redisPort")
                } else {
                    println("Failed to get container ID. Check if docker is running.")
                }
            } catch (e: Exception) {
                println("Failed to start Redis container: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun stopRedis() {
            redisContainerId?.let {
                println("Stopping Redis container: $it")
                ProcessBuilder("docker", "rm", "-f", it).start().waitFor()
            }
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { "localhost" }
            registry.add("spring.data.redis.port") { redisPort }
        }
    }
}
