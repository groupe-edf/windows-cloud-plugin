package fr.edf.jenkins.plugins.windows.http

import java.security.SecureRandom

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import org.apache.http.Header
import org.apache.http.HttpException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.protocol.HTTP

import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.util.Constants
import fr.edf.jenkins.plugins.windows.util.WindowsCloudUtils
import groovy.json.JsonSlurper
import hudson.util.Secret

class MicroserviceHttpClient {

    /** http */
    public static final String PROTOCOL_HTTP = "http"
    /** https */
    public static final String PROTOCOL_HTTPS = "https"

    /** application/json */
    private static final String JSON_CONTENT_TYPE = "application/json"

    /** array of success status of an HTTP response */
    private static final List<Integer> SUCCESS_STATUS = [200, 201, 202, 204]

    /** token */
    private static final String TOKEN_HEADER = "token"

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

    public ExecutionResult whoami() throws HttpException {
        HttpGet request = new HttpGet(url.toString().concat("/api/whoami"))
        Header contentTypeHeader = new BasicHeader(HTTP.CONTENT_TYPE, JSON_CONTENT_TYPE)
        Header tokenHeader = new BasicHeader(TOKEN_HEADER, token.getPlainText())
        request.addHeader(contentTypeHeader)
        request.addHeader(tokenHeader)

        CloseableHttpResponse response = httpClient.execute(request)
        checkResponse(response)
        return new ExecutionResult(new JsonSlurper().parse(response.getEntity().getContent()))
    }

    public ExecutionResult listUser() throws HttpException {
        HttpGet request = new HttpGet(url.toString().concat("/api/users/list"))
        Header contentTypeHeader = new BasicHeader(HTTP.CONTENT_TYPE, JSON_CONTENT_TYPE)
        Header tokenHeader = new BasicHeader(TOKEN_HEADER, token.getPlainText())
        request.addHeader(contentTypeHeader)
        request.addHeader(tokenHeader)

        CloseableHttpResponse response = httpClient.execute(request)
        checkResponse(response)
        return new ExecutionResult(new JsonSlurper().parse(response.getEntity().getContent()))
    }

    public ExecutionResult createUser(WindowsUser user) throws HttpException {
        HttpPost request = new HttpPost(url.toString().concat("/api/user/create"))
        Header contentTypeHeader = new BasicHeader(HTTP.CONTENT_TYPE, JSON_CONTENT_TYPE)
        Header tokenHeader = new BasicHeader(TOKEN_HEADER, token.getPlainText())
        request.addHeader(contentTypeHeader)
        request.addHeader(tokenHeader)
        request.setEntity(new StringEntity("{\"username\":\"$user.username\", \"password\":\"$user.password.plainText\"}"))

        CloseableHttpResponse response = httpClient.execute(request)
        checkResponse(response)
        return new ExecutionResult(new JsonSlurper().parse(response.getEntity().getContent()))
    }

    public ExecutionResult deleteUser(String username) throws HttpException {
        HttpPost request = new HttpPost(url.toString().concat("/api/user/delete?username=$username"))
        Header contentTypeHeader = new BasicHeader(HTTP.CONTENT_TYPE, JSON_CONTENT_TYPE)
        Header tokenHeader = new BasicHeader(TOKEN_HEADER, token.getPlainText())
        request.addHeader(contentTypeHeader)
        request.addHeader(tokenHeader)

        CloseableHttpResponse response = httpClient.execute(request)
        checkResponse(response)
        return new ExecutionResult(new JsonSlurper().parse(response.getEntity().getContent()))
    }

    public ExecutionResult getRemoting(WindowsUser user, String jenkinsUrl) throws HttpException {
        jenkinsUrl = WindowsCloudUtils.checkJenkinsUrl(jenkinsUrl)
        String remotingUrl = jenkinsUrl + Constants.REMOTING_JAR_URL
        HttpPost request = new HttpPost(url.toString().concat("/api/user/remoting?remotingUrl=$remotingUrl"))
        Header contentTypeHeader = new BasicHeader(HTTP.CONTENT_TYPE, JSON_CONTENT_TYPE)
        Header tokenHeader = new BasicHeader(TOKEN_HEADER, token.getPlainText())
        request.addHeader(contentTypeHeader)
        request.addHeader(tokenHeader)
        request.setEntity(new StringEntity("{\"username\":\"$user.username\", \"password\":\"$user.password.plainText\"}"))

        CloseableHttpResponse response = httpClient.execute(request)
        checkResponse(response)
        return new ExecutionResult(new JsonSlurper().parse(response.getEntity().getContent()))
    }

    public ExecutionResult connectJnlp(WindowsUser user, String jenkinsUrl, String secret) throws HttpException {
        jenkinsUrl = WindowsCloudUtils.checkJenkinsUrl(jenkinsUrl)
        HttpPost request = new HttpPost(url.toString().concat("/api/user/jnlp"))
        Header contentTypeHeader = new BasicHeader(HTTP.CONTENT_TYPE, JSON_CONTENT_TYPE)
        Header tokenHeader = new BasicHeader(TOKEN_HEADER, token.getPlainText())
        request.addHeader(contentTypeHeader)
        request.addHeader(tokenHeader)
        request.setEntity(new StringEntity("{\"jenkinsUrl\":\"$jenkinsUrl\",\"secret\":\"$secret\",\"user\":{\"username\":\"$user.username\", \"password\":\"$user.password.plainText\"}}"))

        CloseableHttpResponse response = httpClient.execute(request)
        checkResponse(response)
        return new ExecutionResult(new JsonSlurper().parse(response.getEntity().getContent()))
    }

    private void checkResponse(CloseableHttpResponse response) throws HttpException {
        if(!SUCCESS_STATUS.contains(response.statusLine.statusCode)) {
            throw new HttpException(response.statusLine.statusCode + " : " + response.statusLine.reasonPhrase)
        }
    }
}
