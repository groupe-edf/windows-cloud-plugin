package fr.edf.jenkins.plugins.windows.http

import java.security.SecureRandom

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import org.apache.http.client.HttpClient
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder

import fr.edf.jenkins.plugins.windows.winrm.client.WinRMException
import hudson.util.Secret

class MicroserviceHttpClient {

    /** http */
    public static final String PROTOCOL_HTTP = "http"
    /** https */
    public static final String PROTOCOL_HTTPS = "https"

    URL url

    /** token to access to the microservice */
    Secret token
    /** "true" to ignore https certificate error. */
    boolean disableCertificateChecks
    /** "http" or "https", usage of static constants recommended. */
    boolean useHttps
    /** timeout of the connection in second */
    Integer connectionTimeout = 15
    /** timeout to receive response in second */
    Integer readTimeout = 15

    HttpClient client

    MicroserviceHttpClient(String host, Integer port, String contextPath, Secret token, boolean disableCertificateChecks, boolean useHttps, Integer connectionTimeout, Integer readTimeout) {
        this.url = buildUrl(useHttps?PROTOCOL_HTTPS:PROTOCOL_HTTP, host, port, contextPath)
        this.token = token
        this.disableCertificateChecks = disableCertificateChecks
        this.useHttps = useHttps
        this.connectionTimeout = connectionTimeout
        this.readTimeout = readTimeout
    }

    /**
     * Build the HttpClient or return the existing one
     *
     * @return {@link HttpClient}
     */
    private HttpClient getHttpClient() {
        HttpClientBuilder builder = new HttpClientBuilder()
        if(useHttps) {
            if(disableCertificateChecks) {
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

    /**
     * Creates {@link URL} object to connect to the microservice
     *
     * @param protocol http or https
     * @param address remote host name or ip address
     * @param port port to remote host connection
     * @return created {@link URL} object
     * @throws WinRMException if invalid WinRM URL
     */
    private URL buildUrl(String protocol, String address, int port, String contextPath) {
        return new URL(protocol, address, port, contextPath)
    }
}
