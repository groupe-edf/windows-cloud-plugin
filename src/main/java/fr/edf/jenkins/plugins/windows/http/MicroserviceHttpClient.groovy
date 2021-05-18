package fr.edf.jenkins.plugins.windows.http

import java.security.SecureRandom

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import org.apache.http.Header
import org.apache.http.HttpException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.protocol.HTTP

import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.util.Constants
import fr.edf.jenkins.plugins.windows.util.WindowsCloudUtils
import hudson.util.Secret

/**
 * Client for powershell-daemon apis
 * 
 * @author Mathieu Delrocq
 *
 */
class MicroserviceHttpClient {

    /** http */
    public static final String PROTOCOL_HTTP = "http"
    /** https */
    public static final String PROTOCOL_HTTPS = "https"

    /** application/json */
    private static final String JSON_CONTENT_TYPE = "application/json"

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
    protected HttpClient getHttpClient() {
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

    /**
     * Call whoami api
     * 
     * @return {@link ExecutionResult} with the current user connected as output
     * @throws HttpException
     */
    public ExecutionResult whoami() throws HttpException {
        HttpGet request = new HttpGet(url.toString().concat("/api/whoami"))
        return performRequest(request)
    }

    /**
     * Call list users api
     * 
     * @return {@link ExecutionResult} with the list of users start with "windows-" as output
     * @throws HttpException
     */
    public ExecutionResult listUser() throws HttpException {
        HttpGet request = new HttpGet(url.toString().concat("/api/users/list"))
        return performRequest(request)
    }

    /**
     * Call Create user api
     * 
     * @param user to be created
     * @return {@link ExecutionResult}
     * @throws HttpException
     */
    public ExecutionResult createUser(WindowsUser user) throws HttpException {
        HttpPost request = new HttpPost(url.toString().concat("/api/user/create"))
        request.setEntity(new StringEntity("{\"username\":\"$user.username\", \"password\":\"$user.password.plainText\"}"))
        return performRequest(request)
    }

    /**
     * Call Delete user api
     * 
     * @param username to be deleted
     * @return {@link ExecutionResult}
     * @throws HttpException
     */
    public ExecutionResult deleteUser(String username) throws HttpException {
        HttpPost request = new HttpPost(url.toString().concat("/api/user/delete?username=$username"))
        return performRequest(request)
    }

    /**
     * Call get remoting api
     * 
     * @param user
     * @param jenkinsUrl
     * @return {@link ExecutionResult}
     * @throws HttpException
     */
    public ExecutionResult getRemoting(WindowsUser user, String jenkinsUrl) throws HttpException {
        jenkinsUrl = WindowsCloudUtils.checkJenkinsUrl(jenkinsUrl)
        String remotingUrl = jenkinsUrl + Constants.REMOTING_JAR_URL
        HttpPost request = new HttpPost(url.toString().concat("/api/user/remoting?remotingUrl=$remotingUrl"))
        request.setEntity(new StringEntity("{\"username\":\"$user.username\", \"password\":\"$user.password.plainText\"}"))
        return performRequest(request)
    }

    /**
     * Connect JNLP api
     * 
     * @param user
     * @param jenkinsUrl
     * @param secret
     * @return {@link ExecutionResult}
     * @throws HttpException
     */
    public ExecutionResult connectJnlp(WindowsUser user, String jenkinsUrl, String secret) throws HttpException {
        jenkinsUrl = WindowsCloudUtils.checkJenkinsUrl(jenkinsUrl)
        HttpPost request = new HttpPost(url.toString().concat("/api/user/jnlp"))
        request.setEntity(new StringEntity("{\"jenkinsUrl\":\"$jenkinsUrl\",\"secret\":\"$secret\",\"user\":{\"username\":\"$user.username\", \"password\":\"$user.password.plainText\"}}"))
        return performRequest(request)
    }

    /**
     * Perform the given request and build the response <br>
     * Close HttpClient and HttpResponse after performed
     * 
     * @param request
     * @return {@link ExecutionResult}
     * @throws HttpException if a bad status is returned by the request
     */
    private ExecutionResult performRequest(HttpUriRequest request) throws HttpException {
        CloseableHttpClient client = getHttpClient()
        try {
            Header contentTypeHeader = new BasicHeader(HTTP.CONTENT_TYPE, JSON_CONTENT_TYPE)
            Header tokenHeader = new BasicHeader(TOKEN_HEADER, token.getPlainText())
            request.addHeader(contentTypeHeader)
            request.addHeader(tokenHeader)
            return client.execute(request, new ExecutionResultResponseHandler())
        } catch(Exception e) {
            throw new HttpException("An error occured when performing the request $request.URI", e)
        }
        finally {
            client.close()
        }
    }
}
