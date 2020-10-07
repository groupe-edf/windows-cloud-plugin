package fr.edf.jenkins.plugins.windows.winrm

import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.util.Constants
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionConfiguration
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMGlobalConnectionConfiguration
import jenkins.model.Jenkins
import hudson.util.Secret

class WinRMCommand {
    
    
    @Restricted(NoExternalUse)
    static String checkConnection(WinRMGlobalConnectionConfiguration config) {
        return WinRMCommandLauncher.executeCommand(config, Constants.WHOAMI)
    }
    
    static WindowsUser createUser(WindowsHost host, WindowsUser user) throws WinRMCommandException{
        
        try {
            WinRMGlobalConnectionConfiguration config = new WinRMGlobalConnectionConfiguration(credentialsId: host.credentialsId,
                context: Jenkins.get(), host: host.host, port: host.port, connectionTimeout: host.connectionTimeout,
                authenticationScheme: host.authenticationScheme, useHttps: host.useHttps)
            
            if(doesUserExist(config, user.username)) {
                throw new Exception(String.format("The user %s already exists", user.username))
            }
            return WinRMCommandLauncher.executeCommand(config, String.format(Constants.CREATE_USER, user.username, user.password))
        } catch(Exception e) {
            throw new WinRMCommandException(e.getMessage(), e)
        }
        
    }
    
    private static boolean doesUserExist(WinRMConnectionConfiguration config, String username) throws Exception{
        String res = WinRMCommandLauncher.executeCommand(config, String.format(Constants.CHECK_USER_EXIST, username))
        return res.trim() == username
    }
}
