package fr.edf.jenkins.plugins.windows.winrm.connection

import org.apache.http.client.config.AuthSchemes
import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse

import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials

import io.cloudsoft.winrm4j.client.WinRmClientContext
import io.cloudsoft.winrm4j.winrm.WinRmTool.Builder

class WinRMConnectionFactory {
    
    static Builder getWinRMConnection(WinRMConnectionConfiguration conf) {
        if (conf instanceof WinRMGlobalConnectionConfiguration) {
            return getGlobalWinRMConnection(conf)
        }
        if(conf instanceof WinRMUserConnectionConfiguration) {
            return getUserConnection(conf)
        }
        return null
    }
    
    @Restricted(NoExternalUse)
    private static  Builder getConnection(final StandardCredentials credentials, final String servername, final Integer serverport, 
                                          final String authenticationScheme, final Boolean useHttps) {
                    String address = InetAddress.getByName(servername).toString().split("/")[1]
                    WinRmClientContext context = WinRmClientContext.newInstance()
                    Builder connection = new Builder(address, serverport.intValue(), AuthSchemes.NTLM, useHttps)
                    if(credentials instanceof StandardUsernamePasswordCredentials) {
                        StandardUsernamePasswordCredentials usernamePasswordCredentials = credentials
                        connection.authenticateWithPassword(usernamePasswordCredentials.getUsername(), usernamePasswordCredentials.getPassword().getPlainText())
                        connection.context(context)
                        connection.build()
                        context.shutdown()
                    }
                    return connection
    }
}
