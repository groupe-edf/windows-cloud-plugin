package fr.edf.jenkins.plugins.windows.winrm.connection

import org.apache.http.client.config.AuthSchemes
import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse

import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials

import fr.edf.jenkins.plugins.windows.util.CredentialsUtils
import io.cloudsoft.winrm4j.client.WinRmClientContext
import io.cloudsoft.winrm4j.winrm.WinRmTool
import jenkins.model.Jenkins


class WinRMConnectionFactory {

    /**
     * Dispatch request on the good method of connection
     * @param config
     * @return WinRmTool
     * @throws WinRMConnectionException
     */
    static WinRmTool getWinRMConnection(WinRMConnectionConfiguration config, WinRmClientContext winRMContext) throws WinRMConnectionException{
        if(config instanceof WinRMGlobalConnectionConfiguration) {
            return getGlobalWinRMConnection(config, winRMContext)
        }

        return null
    }

    /**
     * 
     * @param config
     * @return
     * @throws WinRMConnectionException
     */

    @Restricted(NoExternalUse)
    private static WinRmTool getGlobalWinRMConnection(WinRMGlobalConnectionConfiguration config = new WinRMGlobalConnectionConfiguration(), WinRmClientContext winRMContext) throws WinRMConnectionException {
        String host = config.host
        Integer port = config.port ?: Integer.valueOf(5985)
        Integer connectionTimeout = config.connectionTimeout ?: Integer.valueOf(1000)
        String authenticationScheme = config.authenticationScheme ?: AuthSchemes.NTLM
        Boolean useHttps = config.useHttps ?: Boolean.FALSE
        def context = config.context ?: Jenkins.get()
        def credentialsId = config.credentialsId ?: null
        if(!credentialsId) {
            throw new WinRMConnectionException("No credentials found for the host " + host)
        }
        def credentials = CredentialsUtils.findCredentials(host, credentialsId, context)
        return getConnection(host, credentials, port, authenticationScheme, useHttps, winRMContext)
    }

    /**
     * 
     * @param host : hostname of the windows machine
     * @param credentials
     * @param port
     * @param useHttps
     * @return
     * @throws WinRMConnectionException
     */

    @Restricted(NoExternalUse)
    private static WinRmTool getConnection(final String host, final StandardCredentials credentials, final Integer port,
            final String authenticationScheme, final Boolean useHttps, WinRmClientContext winRMContext) throws WinRMConnectionException {
        if (credentials instanceof StandardUsernamePasswordCredentials) {
            StandardUsernamePasswordCredentials usernamePasswordCredentials = credentials
            WinRmTool winRmTool = WinRmTool.Builder.builder(host, usernamePasswordCredentials.getUsername(),
                    usernamePasswordCredentials.getPassword().getPlainText())
                    .authenticationScheme(AuthSchemes.NTLM)
                    .port(port.intValue())
                    .useHttps(useHttps.booleanValue())
                    .disableCertificateChecks(true)
                    .context(winRMContext)
                    .build();
            winRmTool.setConnectionTimeout(10000)
            winRmTool.setReceiveTimeout(10000)
            return winRmTool
        } else {
            throw new WinRMConnectionException("Only Username and Password Credentials are allowed")
        }
    }
}
