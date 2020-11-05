/*
 * Copyright 2020, EDF Group and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.edf.jenkins.plugins.windows.winrm.client

import java.nio.charset.Charset
import java.security.SecureRandom
import java.util.logging.Level
import java.util.logging.Logger

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import org.apache.commons.lang.StringUtils
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.auth.AuthSchemeProvider
import org.apache.http.auth.AuthScope
import org.apache.http.auth.NTCredentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.config.AuthSchemes
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicSchemeFactory
import org.apache.http.impl.auth.KerberosSchemeFactory
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HTTP
import org.apache.http.protocol.HttpContext

import fr.edf.jenkins.plugins.windows.winrm.client.auth.ntlm.SpNegoNTLMSchemeFactory
import fr.edf.jenkins.plugins.windows.winrm.client.auth.spnego.WsmanSPNegoSchemeFactory
import fr.edf.jenkins.plugins.windows.winrm.client.output.CommandOutput
import fr.edf.jenkins.plugins.windows.winrm.client.request.CleanupCommandRequest
import fr.edf.jenkins.plugins.windows.winrm.client.request.DeleteShellRequest
import fr.edf.jenkins.plugins.windows.winrm.client.request.ExecuteCommandRequest
import fr.edf.jenkins.plugins.windows.winrm.client.request.GetCommandOutputRequest
import fr.edf.jenkins.plugins.windows.winrm.client.request.OpenShellRequest
import fr.edf.jenkins.plugins.windows.winrm.client.request.WinRMRequest
import groovy.util.slurpersupport.GPathResult

/**
 * Allow Jenkins to launch PowerShell commands on a windows remote machine
 * @author Mathieu Delrocq
 *
 */
class WinRMTool {

    static final Logger LOGGER = Logger.getLogger(WinRMTool.name)
    public static final String PROTOCOL_HTTP = "http"
    public static final String PROTOCOL_HTTPS = "https"
    private static final String SOAP_REQUEST_CONTENT_TYPE = "application/soap+xml; charset=UTF-8"
    private static final String WSMAN_ROOT_URI = "/wsman"
    private static final String TLS = "TLS"
    final List<Integer> sucessStatus = [200, 201, 202, 204]

    /** username to connect  with. */
    String username
    /** password associated to the user. */
    String password
    /** @see AuthSchemes. */
    String authSheme
    /** windos domain of the machine (**optional value for ntlm authentication**). */
    String domain
    /** name of the windows machine (**optional value for ntlm authentication**). */
    String workstation
    /** "true" to ignore https certificate error. */
    boolean disableCertificateChecks
    /** "http" or "https", usage of static constants recommended. */
    boolean useHttps
    /** timeout of the command */
    Integer commandTimeout = 60
    /** timeout of the connection */
    Integer connectionTimeout = 20
    /** timeout to receive response */
    Integer readTimeout = 60

    URL url
    String lastShellId
    String lastCommandId
    HttpClient httpClient

    /**
     * Default constructor for {@link WinRMTool}
     * @param address : ip adress or hostname
     * @param port : port to connect
     * @param username : username used for the connection
     * @param password : password of the user
     * @param authSheme : {@link AuthSchemes} used to connect, only NTLM or BASIC are allowed in this client
     * @param useHttps : <code>true</code> if the connection use https, <code>false</code> elsewhere
     * @param disableCertificateChecks : <code>true</code> if you want to ignore certificate check
     * @param timeout : delay before the command have to respond
     */
    WinRMTool(String address, int port, String username, String password, String authSheme, boolean useHttps,
    boolean disableCertificateChecks, Integer commandTimeout) {
        this.url = buildUrl(useHttps?PROTOCOL_HTTPS:PROTOCOL_HTTP,address,port)
        this.username = username
        this.password = password
        this.authSheme = authSheme
        this.useHttps = useHttps
        this.disableCertificateChecks = disableCertificateChecks
        this.commandTimeout = commandTimeout
    }

    /**
     * Open a shell on the remote machine.
     * 
     * @return Shell ID
     * @throws {@link WinRMException}
     */
    String openShell() throws WinRMException {
        HttpClient httpClient = getHttpClient()
        HttpPost httpPost = buildHttpPostRequest(new OpenShellRequest(url, commandTimeout))
        HttpContext context = buildHttpContext()
        HttpResponse response = performRequest(httpPost, context)
        StatusLine status = response.getStatusLine()
        int responseCode = status.getStatusCode()
        if(!sucessStatus.contains(responseCode)) {
            throw new WinRMException(String.format(
            WinRMException.FORMATTED_MESSAGE,
            "OpenShell",
            status.getProtocolVersion(),
            responseCode,
            status.getReasonPhrase()))
        }
        HttpEntity responseEntity = response.getEntity();
        String responseBody = responseEntity?.getContent()?.text
        LOGGER.log(Level.FINEST, "RESPONSE BODY :" + responseBody)
        GPathResult results = new XmlSlurper().parseText(responseBody)
        String shellId = results?.'*:Body'?.'*:Shell'?.'*:ShellId'
        if(StringUtils.isEmpty(shellId)) {
            throw new WinRMException(String.format(
            WinRMException.FORMATTED_MESSAGE,
            "OpenShell",
            status.getProtocolVersion(),
            responseCode,
            "Cannot retrieve the shell id in the given response :" + responseBody))
        }
        this.lastShellId = shellId
        return shellId
    }

    /**
     * Compile PS and call executeCommand. <br/>
     * A Shell must be opened
     * 
     * @return commandId
     * @throws WinRMException with code and message if an error occured
     */
    String executePSCommand(String shellId = lastShellId, String psCommand, String[] args = []) throws WinRMException {
        return executeCommand(shellId, compilePs(psCommand), args)
    }

    /**
     * Execute a command on the remote machine. <br/>
     * A Shell must be opened
     * 
     * @return commandId
     * @throws WinRMException with code and message if an error occured
     */
    String executeCommand(String shellId = lastShellId, String commandLine, String[] args = []) throws WinRMException {
        if(StringUtils.isEmpty(shellId)) {
            throw new WinRMException("Call openShell() before execute command")
        }
        HttpClient httpClient = getHttpClient()
        HttpPost httpPost = buildHttpPostRequest(new ExecuteCommandRequest(url, shellId, commandLine, args, commandTimeout))
        HttpContext context = buildHttpContext()
        HttpResponse response = performRequest(httpPost, context)
        StatusLine status = response.getStatusLine()
        int responseCode = status.getStatusCode()
        if(!sucessStatus.contains(responseCode)) {
            throw new WinRMException(String.format(
            WinRMException.FORMATTED_MESSAGE,
            "ExecuteCommand",
            status.getProtocolVersion(),
            responseCode,
            status.getReasonPhrase()))
        }
        HttpEntity responseEntity = response.getEntity();
        String responseBody = responseEntity.getContent().text
        LOGGER.log(Level.FINEST, "RESPONSE BODY :" + responseBody)
        GPathResult results = new XmlSlurper().parseText(responseBody)
        String commandId = results?.'*:Body'?.'*:CommandResponse'?.'*:CommandId'
        if(StringUtils.isEmpty(commandId)) {
            throw new WinRMException(String.format(
            WinRMException.FORMATTED_MESSAGE,
            "ExecuteCommand",
            status.getProtocolVersion(),
            responseCode,
            "Cannot retrieve the command id in the given response : ") + responseBody)
        }
        this.lastCommandId = commandId
        return commandId
    }

    /**
     * Return the output for the given command id <br/>
     * A command must be launched befrore call this method
     * 
     * @param shellId
     * @param commandId
     * @return {@link CommandOutput}
     * @throws WinRMException with code and message if an error occured
     */
    CommandOutput getCommandOutput(String shellId = lastShellId, String commandId = lastCommandId) throws WinRMException {
        if(StringUtils.isEmpty(commandId)) {
            throw new WinRMException("No command was executed")
        }
        HttpClient httpClient = getHttpClient()
        HttpPost httpPost = buildHttpPostRequest(new GetCommandOutputRequest(url, shellId, commandId, commandTimeout))
        HttpContext context = buildHttpContext()
        HttpResponse response = performRequest(httpPost, context)
        StatusLine status = response.getStatusLine()
        int responseCode = status.getStatusCode()
        if(!sucessStatus.contains(responseCode)) {
            throw new WinRMException(String.format(
            WinRMException.FORMATTED_MESSAGE,
            "GetCommandOutput",
            status.getProtocolVersion(),
            responseCode,
            status.getReasonPhrase()))
        }
        HttpEntity responseEntity = response.getEntity();
        String responseBody = responseEntity.getContent().text
        LOGGER.log(Level.FINEST, "RESPONSE BODY :" + responseBody)
        GPathResult results = new XmlSlurper().parseText(responseBody)

        String output = ''
        String error = ''

        results?.'*:Body'?.'*:ReceiveResponse'?.'*:Stream'?.findAll {
            it.@Name == 'stdout' && it.@CommandId == commandId
        }?.each { output += new String(it.toString()?.decodeBase64()) }

        results?.'*:Body'?.'*:ReceiveResponse'?.'*:Stream'?.findAll {
            it.@Name == 'stderr' && it.@CommandId == commandId
        }?.each { error += new String(it.toString()?.decodeBase64()) }

        if (results?.'*:Body'?.'*:ReceiveResponse'?.'*:CommandState'?.find {
            it.@CommandId == commandId && it.@State == 'http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done'
        }) {
            Long exitStatus = results?.'*:Body'?.'*:ReceiveResponse'?.'*:CommandState'?.'*:ExitCode'?.text()?.toLong()
            LOGGER.log(Level.FINEST, "exitStatus : " + exitStatus)
            LOGGER.log(Level.FINEST, "commandOutput : " + output)
            LOGGER.log(Level.FINEST, "errOutput : " + error)
            return new CommandOutput(exitStatus, output, error)
        } else {
            return new CommandOutput(-1, output, CommandOutput.COMMAND_STILL_RUNNING)
        }
    }

    /**
     * Terminate the command with the given id <br/>
     * A command must be launch before call this method
     * 
     * @param shellId
     * @param commandId
     * @throws WinRMException with code and message if an error occured
     */
    void cleanupCommand(String shellId = lastShellId, String commandId = lastCommandId) throws WinRMException {
        if(StringUtils.isEmpty(commandId)) {
            throw new WinRMException("No command was executed")
        }
        HttpClient httpClient = getHttpClient()
        HttpPost httpPost = buildHttpPostRequest(new CleanupCommandRequest(url, shellId, commandId, commandTimeout))
        HttpContext context = buildHttpContext()
        HttpResponse response = performRequest(httpPost, context)
        StatusLine status = response.getStatusLine()
        int responseCode = status.getStatusCode()
        if(!sucessStatus.contains(responseCode)) {
            throw new WinRMException(String.format(
            WinRMException.FORMATTED_MESSAGE,
            "CleanupCommand",
            status.getProtocolVersion(),
            responseCode,
            status.getReasonPhrase()))
        }
        LOGGER.log(Level.FINEST, "RESPONSE BODY :" + response.getEntity().getContent().text)
    }

    /**
     * Close the shell on the remote machine
     * 
     * @param shellId
     * @throws WinRMException with code and message if an error occured
     */
    void deleteShellRequest(String shellId = lastShellId) throws WinRMException {
        if(StringUtils.isEmpty(shellId)) {
            throw new WinRMException("There is no shell Id")
        }
        HttpClient httpClient = getHttpClient()
        HttpPost httpPost = buildHttpPostRequest(new DeleteShellRequest(url, shellId, commandTimeout))
        HttpContext context = buildHttpContext()
        HttpResponse response = performRequest(httpPost, context)
        StatusLine status = response.getStatusLine()
        int responseCode = status.getStatusCode()
        if(!sucessStatus.contains(responseCode)) {
            throw new WinRMException(String.format(
            WinRMException.FORMATTED_MESSAGE,
            "DeleteShellRequest",
            status.getProtocolVersion(),
            responseCode,
            status.getReasonPhrase()))
        }
        LOGGER.log(Level.FINEST, "RESPONSE BODY :" + response.getEntity().getContent().text)
    }

    /**
     * Perform the request with exception management
     * 
     * @param httpPost
     * @param context
     * @return {@link HttpResponse}
     * @throws WinRMException
     */
    private HttpResponse performRequest(HttpPost httpPost, HttpContext context) throws WinRMException {
        HttpResponse response = null
        try {
            response = httpClient.execute(httpPost, context)
        } catch(Exception e) {
            throw new WinRMException("Cannot perform request due to unexpected exception : " + e.getLocalizedMessage(), e)
        }
        return response
    }

    /**
     * Build the HttpClient or return the existing one
     * 
     * @return {@link HttpClient}
     */
    private HttpClient getHttpClient() {
        if (null == httpClient) {
            HttpClientBuilder builder = new HttpClientBuilder()
            if(useHttps) {
                if(disableCertificateChecks) {
                    builder.setSSLContext(buildIgnoreCertificateErrorContext())
                    builder.setSSLHostnameVerifier(buildIgnoreHostnameVerifier())
                }
            }
            builder.setDefaultAuthSchemeRegistry(buildAuthShemeRegistry(authSheme))
            CloseableHttpClient httpclient = builder.build()
            this.httpClient = httpclient
        }
        return httpClient
    }

    /**
     * Build the http post request
     * 
     * @param request : type of request
     * @return {@link HttpPost}
     */
    private HttpPost buildHttpPostRequest(WinRMRequest request) {
        // Init HttpPost
        HttpPost httpPost = new HttpPost(url.toURI())
        try {
            // Build request entity
            String requestString = request.toString()
            LOGGER.log(Level.FINEST, "REQUEST BODY :" + requestString)
            StringEntity entity = new StringEntity(requestString)
            Header contentTypeHeader = new BasicHeader(HTTP.CONTENT_TYPE, SOAP_REQUEST_CONTENT_TYPE)
            entity.setContentType(contentTypeHeader)
            httpPost.setEntity(entity)
            // Request config
            RequestConfig.Builder configBuilder = RequestConfig.custom()
            .setConnectionRequestTimeout(connectionTimeout.intValue()*1000)
            .setSocketTimeout(readTimeout.intValue()*1000)
            httpPost.setConfig(configBuilder.build())
        }catch (Exception e) {
            throw new WinRMException("Cannot build HttpPost request due to unexpected exception : " + e.getLocalizedMessage(), e)
        }
        return httpPost
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
            sslContext = SSLContext.getInstance(TLS)
            sslContext.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], new SecureRandom())
        }catch(Exception e) {
            throw new WinRMException("Cannot init SSLContext due to unexpected exception : " + e.getLocalizedMessage(), e)
        }
        return sslContext
    }

    /**
     * Return the HostNameVerifier of the remote machine.<br/>
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
     * Build and return AuthShemeRegistry
     * 
     * @param authenticationScheme
     * @return {@link Registry<AuthSchemeProvider>}
     */
    private Registry<AuthSchemeProvider> buildAuthShemeRegistry(String authenticationScheme) {
        Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                .register(AuthSchemes.SPNEGO,
                authenticationScheme.equals(AuthSchemes.NTLM) ? new SpNegoNTLMSchemeFactory() : new WsmanSPNegoSchemeFactory())
                .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())//
                .build()
        return authSchemeRegistry
    }

    /**
     * Build and return the http context with the authentication
     * 
     * @return {@link HttpContext}
     * @throws WinRMException if invalid authentication scheme
     */
    private HttpContext buildHttpContext() throws WinRMException {
        HttpContext localContext = new BasicHttpContext();
        CredentialsProvider credsProvider = new BasicCredentialsProvider()
        switch(authSheme) {
            case AuthSchemes.BASIC:
                credsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password))
                break
            case AuthSchemes.NTLM:
                credsProvider.setCredentials(AuthScope.ANY,
                new NTCredentials(username, password, workstation, domain))
                break
            default:
                throw new WinRMException("No such authentication scheme " + authSheme)
        }
        localContext.setAttribute(HttpClientContext.CREDS_PROVIDER, credsProvider)
        return localContext
    }

    /**
     * Compile PowerShell script
     * 
     * @param psScript
     * @return encoded PowerShell
     */
    private String compilePs(String psScript) {
        byte[] cmd = psScript.getBytes(Charset.forName("UTF-16LE"))
        String arg = cmd.encodeBase64().toString()
        return "powershell -encodedcommand " + arg
    }

    /**
     * Creates {@link URL} object to connect to remote host by WinRM
     *
     * @param protocol http or https
     * @param address remote host name or ip address
     * @param port port to remote host connection
     * @return created {@link URL} object
     * @throws WinRMException if invalid WinRM URL
     */
    private URL buildUrl(String protocol, String address, int port) throws WinRMException {
        try {
            new URL(protocol, address, port, WSMAN_ROOT_URI)
        } catch (MalformedURLException e) {
            throw new WinRMException("Invalid WinRM URL!", e)
        }
    }
}
