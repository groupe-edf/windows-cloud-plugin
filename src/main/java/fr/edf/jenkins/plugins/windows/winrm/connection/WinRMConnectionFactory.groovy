package fr.edf.jenkins.plugins.windows.winrm.connection

import org.apache.http.client.config.AuthSchemes
import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse

import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials

import fr.edf.jenkins.plugins.windows.util.CredentialsUtils
import fr.edf.jenkins.plugins.windows.util.FormUtils
import io.cloudsoft.winrm4j.winrm.WinRmTool
import jenkins.model.Jenkins


class WinRMConnectionFactory {

    /**
     * Dispatch request on the good method of connection
     * @param config
     * @return WinRmTool
     * @throws WinRMConnectionException
     */
    static WinRmTool getWinRMConnection(WinRMConnectionConfiguration config) throws WinRMConnectionException{
        if(config instanceof WinRMGlobalConnectionConfiguration) {
            return getGlobalWinRMConnection(config)
        }
//                if(config instanceof WinRMUserConnectionConfiguration) {
//                    return getUserWinRMConnection(config)
//                }
        return null
    }

    /**
     * 
     * @param config
     * @return
     * @throws WinRMConnectionException
     */
    
    @Restricted(NoExternalUse)
    private static WinRmTool getGlobalWinRMConnection(WinRMGlobalConnectionConfiguration config = new WinRMGlobalConnectionConfiguration()) throws WinRMConnectionException {
        String host = config.host
        Integer port = config.port ?: Integer.valueOf(5985)
        String authenticationScheme = config.authenticationScheme ?: AuthSchemes.NTLM
        Boolean useHttps = config.useHttps ?: Boolean.FALSE
        def context = config.context ?: Jenkins.get()
        def credentialsId = config.credentialsId ?: null
        if(!credentialsId) {
            throw new WinRMConnectionException("No credentials found for the host " + host)
        }
        def credentials = CredentialsUtils.findCredentials(host, credentialsId, context)
        return getConnection(host, credentials, port, authenticationScheme, useHttps)
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
    final String authenticationScheme, final Boolean useHttps) throws WinRMConnectionException {
        WinRmTool tool = null
        if (credentials instanceof StandardUsernamePasswordCredentials) {
            StandardUsernamePasswordCredentials usernamePasswordCredentials = credentials
            tool = WinRmTool.Builder.builder(host, usernamePasswordCredentials.getUsername(), usernamePasswordCredentials.getPassword().getPlainText())
            .authenticationScheme(AuthSchemes.NTLM)
            .port(5985)
            .useHttps(false)
            .build()
        } else {
            throw new WinRMConnectionException("Only Username and Password Credentials are allowed")
        }
        return tool
    }
}
