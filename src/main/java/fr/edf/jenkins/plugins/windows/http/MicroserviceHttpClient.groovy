package fr.edf.jenkins.plugins.windows.http

import java.security.SecureRandom

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import org.apache.http.client.HttpClient
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder

class MicroserviceHttpClient {

    HttpClient client
    boolean useHttps
    boolean ignoreCertificate

    MicroserviceHttpClient(){
    }

    /**
     * Build the HttpClient or return the existing one
     *
     * @return {@link HttpClient}
     */
    private HttpClient getHttpClient() {
        HttpClientBuilder builder = new HttpClientBuilder()
        if(useHttps) {
            if(ignoreCertificate) {
                builder.setSSLContext(buildIgnoreCertificateErrorContext())
                builder.setSSLHostnameVerifier(buildIgnoreHostnameVerifier())
            }
        }
        return builder.build()
    }

    /**
     * Return the HostNameVerifier which ignores hostname check.<br/>
     *
     * @param String
     * @return {@link HostnameVerifier}
     */
    private HostnameVerifier buildIgnoreHostnameVerifier() {
        def nullHostnameVerifier = [
            verify: { hostname, session ->
                true
            }
        ]
        return nullHostnameVerifier as HostnameVerifier
    }

    /**
     * Build a TrustManager wich accept any certificate
     *
     * @return {@link SSLContext}
     */
    private SSLContext buildIgnoreCertificateErrorContext() {
        SSLContext sslContext = null

        def nullTrustManager = [
            checkClientTrusted: { chain, authType ->  },
            checkServerTrusted: { chain, authType ->  },
            getAcceptedIssuers: {
                null
            }
        ]

        try {
            sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], new SecureRandom())
        }catch(Exception e) {
            throw new Exception("Cannot init SSLContext due to unexpected exception : $e.localizedMessage", e)
        }
        return sslContext
    }
}
