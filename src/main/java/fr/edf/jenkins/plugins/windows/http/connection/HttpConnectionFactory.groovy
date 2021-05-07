package fr.edf.jenkins.plugins.windows.http.connection

import org.jenkinsci.plugins.plaincredentials.StringCredentials
import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse

import com.cloudbees.plugins.credentials.common.StandardCredentials

import fr.edf.jenkins.plugins.windows.http.MicroserviceHttpClient
import fr.edf.jenkins.plugins.windows.util.CredentialsUtils
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionException
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMGlobalConnectionConfiguration
import jenkins.model.Jenkins

/**
 * 
 * @author Mathieu Delrocq
 *
 */
class HttpConnectionFactory {

    @Restricted(NoExternalUse)
    public static MicroserviceHttpClient getHttpConnection(HttpConnectionConfiguration config = new WinRMGlobalConnectionConfiguration()) throws WinRMConnectionException {
        String host = config.host
        Integer port = config.port ?: Integer.valueOf(8443)
        Integer connectionTimeout = config.connectionTimeout ?: Integer.valueOf(15)
        Integer readTimeout = config.readTimeout ?: Integer.valueOf(15)
        Boolean useHttps = config.useHttps ?: Boolean.FALSE
        Boolean disableCertificateCheck = config.disableCertificateCheck ?: Boolean.FALSE
        def context = config.context ?: Jenkins.get()
        def credentialsId = config.credentialsId ?: null
        String contextPath = config.contextPath ?: ""
        if(!credentialsId) {
            throw new HttpConnectionException("No credentials found for the host " + host)
        }
        def credentials = CredentialsUtils.findCredentials(host, credentialsId, context)
        return getConnection(host, credentials, port, contextPath, useHttps, disableCertificateCheck, connectionTimeout, readTimeout)
    }

    @Restricted(NoExternalUse)
    private static MicroserviceHttpClient getConnection(final String host, final StandardCredentials credentials, final Integer port,
            final String contextPath, final Boolean useHttps, final Boolean disableCertificateCheck,
            final Integer connectionTimeout, final Integer readTimeout) throws WinRMConnectionException {
        if (credentials instanceof StringCredentials) {
            StringCredentials secretToken = credentials
            MicroserviceHttpClient microserviceHttpClient = new MicroserviceHttpClient(
                    host,
                    port.intValue(),
                    contextPath,
                    secretToken.secret,
                    useHttps.booleanValue(),
                    disableCertificateCheck.booleanValue(),
                    connectionTimeout,
                    readTimeout)
            return microserviceHttpClient
        } else {
            throw new HttpConnectionException("Only Username and Password Credentials are allowed")
        }
    }
}
