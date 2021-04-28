package fr.edf.jenkins.plugins.windows.http.connection

import org.jenkinsci.plugins.plaincredentials.StringCredentials
import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse

import com.cloudbees.plugins.credentials.common.StandardCredentials

import fr.edf.jenkins.plugins.windows.http.MicroserviceConnectionException
import fr.edf.jenkins.plugins.windows.http.MicroserviceHttpClient
import fr.edf.jenkins.plugins.windows.util.CredentialsUtils
import fr.edf.jenkins.plugins.windows.winrm.client.WinRMTool
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionException
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMGlobalConnectionConfiguration
import jenkins.model.Jenkins

class HttpConnectionFactory {

    @Restricted(NoExternalUse)
    private static MicroserviceHttpClient getHttpConnection(HttpConnectionConfiguration config = new WinRMGlobalConnectionConfiguration()) throws WinRMConnectionException {
        String host = config.host
        Integer port = config.port ?: Integer.valueOf(5985)
        Integer connectionTimeout = config.connectionTimeout ?: Integer.valueOf(15)
        Integer readTimeout = config.readTimeout ?: Integer.valueOf(15)
        Boolean useHttps = config.useHttps ?: Boolean.FALSE
        Boolean disableCertificateCheck = config.disableCertificateCheck ?: Boolean.FALSE
        def context = config.context ?: Jenkins.get()
        def credentialsId = config.credentialsId ?: null
        if(!credentialsId) {
            throw new WinRMConnectionException("No credentials found for the host " + host)
        }
        def credentials = CredentialsUtils.findCredentials(host, credentialsId, context)
        return getConnection(host, credentials, port, useHttps, disableCertificateCheck, connectionTimeout, readTimeout)
    }

    @Restricted(NoExternalUse)
    private static MicroserviceHttpClient getConnection(final String host, final StandardCredentials credentials, final Integer port,
            final Boolean useHttps, final Boolean disableCertificateCheck,
            final Integer connectionTimeout, final Integer readTimeout) throws WinRMConnectionException {
        if (credentials instanceof StringCredentials) {
            StringCredentials secretToken = credentials
            MicroserviceHttpClient microserviceHttpClient = new MicroserviceHttpClient(
                    host,
                    port.intValue(),
                    secretToken.secret,
                    useHttps.booleanValue(),
                    disableCertificateCheck.booleanValue(),
                    connectionTimeout,
                    readTimeout)
            return microserviceHttpClient
        } else {
            throw new MicroserviceConnectionException("Only Username and Password Credentials are allowed")
        }
    }
}
