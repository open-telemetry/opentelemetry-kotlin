package io.opentelemetry.kotlin.export

import io.ktor.client.engine.okhttp.OkHttp
import io.opentelemetry.kotlin.ExperimentalApi
import java.io.File
import java.io.FileInputStream
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Configures the default OTLP HTTP client to trust [certificateFile] and optionally present a
 * client certificate and key for mTLS. PEM files are read when the exporter is initialized.
 * The private key must be unencrypted PKCS#8 PEM. A supplied
 * [OtlpHttpExporterConfigDsl.httpClient] takes precedence for requests.
 *
 * Available on JVM and Android. Other targets can supply a TLS-configured `httpClient`.
 */
@ExperimentalApi
public fun OtlpHttpExporterConfigDsl.configureTls(
    certificateFile: String? = null,
    clientKeyFile: String? = null,
    clientCertificateFile: String? = null,
) {
    require((clientKeyFile == null) == (clientCertificateFile == null)) {
        "clientKeyFile and clientCertificateFile must be set together"
    }
    require(certificateFile != null || clientKeyFile != null) {
        "At least one TLS certificate or client key file is required"
    }

    val trustStore = certificateFile?.let { file ->
        KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            readCertificates(file).forEachIndexed { index, certificate ->
                setCertificateEntry("collector-$index", certificate)
            }
        }
    }
    val trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
        init(trustStore)
    }.trustManagers
    val trustManager = trustManagers.filterIsInstance<X509TrustManager>().first()

    val keyManagers = clientKeyFile?.let { keyFile ->
        val certificates = readCertificates(checkNotNull(clientCertificateFile))
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }
        val password = CharArray(0)
        keyStore.setKeyEntry("client", readPrivateKey(keyFile), password, certificates.toTypedArray())
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
            init(keyStore, password)
        }.keyManagers
    }
    val sslContext = SSLContext.getInstance("TLS").apply {
        init(keyManagers, arrayOf(trustManager), null)
    }
    httpClientEngine = OkHttp.create {
        config { sslSocketFactory(sslContext.socketFactory, trustManager) }
    }
}

internal fun readCertificates(file: String): List<X509Certificate> =
    FileInputStream(file).use { input ->
        CertificateFactory.getInstance("X.509").generateCertificates(input)
            .map { it as X509Certificate }
            .also { require(it.isNotEmpty()) { "No certificates found in $file" } }
    }

@OptIn(ExperimentalEncodingApi::class)
internal fun readPrivateKey(file: String): PrivateKey {
    val pem = File(file).readText()
    val begin = "-----BEGIN PRIVATE KEY-----"
    val end = "-----END PRIVATE KEY-----"
    require(begin in pem && end in pem) { "Client key must be unencrypted PKCS#8 PEM" }
    val encoded = pem.substringAfter(begin).substringBefore(end).filterNot(Char::isWhitespace)
    val spec = PKCS8EncodedKeySpec(Base64.decode(encoded))
    for (algorithm in listOf("RSA", "EC", "DSA", "Ed25519")) {
        try {
            return KeyFactory.getInstance(algorithm).generatePrivate(spec)
        } catch (ignored: GeneralSecurityException) {
            // Try the next supported key algorithm.
        }
    }
    throw IllegalArgumentException("Unsupported client key in $file")
}
