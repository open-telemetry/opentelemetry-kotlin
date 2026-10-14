package io.opentelemetry.kotlin.export

import com.sun.net.httpserver.HttpsConfigurator
import com.sun.net.httpserver.HttpsExchange
import com.sun.net.httpserver.HttpsServer
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.security.KeyStore
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OtlpHttpTlsTest {

    @Test
    fun testCustomTrustAndMutualTls() {
        val certificateFile = checkNotNull(javaClass.getResource("/tls/cert.pem")).toURI().path
        val keyFile = checkNotNull(javaClass.getResource("/tls/key.pem")).toURI().path
        val certificate = readCertificates(certificateFile).single()
        val store = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }
        store.setKeyEntry("server", readPrivateKey(keyFile), CharArray(0), arrayOf(certificate))
        val keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
            init(store, CharArray(0))
        }.keyManagers
        val trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(store)
        }.trustManagers
        val context = SSLContext.getInstance("TLS").apply { init(keyManagers, trustManagers, null) }
        val sawClientCertificate = AtomicBoolean()
        val server = HttpsServer.create(InetSocketAddress("localhost", 0), 0).apply {
            httpsConfigurator = object : HttpsConfigurator(context) {
                override fun configure(params: com.sun.net.httpserver.HttpsParameters) {
                    params.setSSLParameters(context.defaultSSLParameters.apply { needClientAuth = true })
                }
            }
            createContext("/v1/traces") { exchange ->
                val httpsExchange = exchange as HttpsExchange
                sawClientCertificate.set(httpsExchange.sslSession.peerCertificates.isNotEmpty())
                exchange.requestBody.use { it.readBytes() }
                exchange.sendResponseHeaders(200, -1)
                exchange.close()
            }
            start()
        }

        try {
            val client = createOtlpHttpClient(NoopSdkErrorHandler) {
                endpoint = "https://localhost:${server.address.port}"
                configureTls(certificateFile, keyFile, certificateFile)
            }
            assertEquals(OtlpResponse.Success, runBlocking { client.exportTraces(listOf(FakeSpanData())) })
            assertTrue(sawClientCertificate.get())
        } finally {
            server.stop(0)
        }
    }
}
