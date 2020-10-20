package fr.edf.jenkins.plugins.windows.winrm

import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang.StringUtils
import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.util.Constants
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionConfiguration
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMGlobalConnectionConfiguration
import hudson.util.Secret
import jenkins.model.Jenkins

class WinRMCommand {


    @Restricted(NoExternalUse)
    static String checkConnection(WinRMGlobalConnectionConfiguration config) {
        return WinRMCommandLauncher.executeCommand(config, Constants.WHOAMI)
    }

    private static boolean doesUserExist(WinRMConnectionConfiguration config, String username) throws Exception{
        String res = WinRMCommandLauncher.executeCommand(config, String.format(Constants.CHECK_USER_EXIST, username))
        return res.trim() == username
    }

    @Restricted(NoExternalUse)
    static WindowsUser generateUser() {
        String username = String.format(Constants.USERNAME_PATTERN, RandomStringUtils.random(15, true, true).toLowerCase())
        String password = RandomStringUtils.random(15, true, true)
        String workdir = String.format(Constants.WORKDIR_PATTERN, username)
        return new WindowsUser(username: username, password: Secret.fromString(password), workdir: workdir)
    }


    @Restricted(NoExternalUse)
    static WindowsUser createUser(WindowsHost host, WindowsUser user) throws WinRMCommandException, Exception{

        try {
            WinRMGlobalConnectionConfiguration config = new WinRMGlobalConnectionConfiguration(credentialsId: host.credentialsId,
            context: Jenkins.get(), host: host.host, port: host.port, connectionTimeout: host.connectionTimeout,
            authenticationScheme: host.authenticationScheme, useHttps: host.useHttps)
            WinRMCommandLauncher.executeCommand(config, String.format(Constants.CREATE_USER, user.username, user.password.getPlainText(), user.username))

            if(!doesUserExist(config, user.username)) {
                throw new Exception(String.format("The user %s already exists", user.username))
            }
            WinRMCommandLauncher.executeCommand(config, String.format(Constants.CREATE_DIR, user.username))
            WinRMCommandLauncher.executeCommand(config, String.format(Constants.DISABLE_INHERITED_WORKDIR, user.username, user.username))
            WinRMCommandLauncher.executeCommand(config, String.format(Constants.GRANT_ACCESS_WORKDIR, user.username, user.username))
            return user
        } catch(Exception e) {
            String message = String.format(WinRMCommandException.CREATE_WINDOWS_USER_ERROR, host.host)
            throw new WinRMCommandException(message, e)
        }
    }

    static void deleteUser(WindowsHost host, String username) throws WinRMCommandException{
        try {
            WinRMGlobalConnectionConfiguration config = new WinRMGlobalConnectionConfiguration(credentialsId: host.credentialsId,
            context: Jenkins.get(), host: host.host, port: host.port, connectionTimeout: host.connectionTimeout,
            authenticationScheme: host.authenticationScheme, useHttps: host.useHttps)

            if(!doesUserExist(config, username)) {
                throw new Exception(String.format("The user %s does not exist", username))
            }
            
            WinRMCommandLauncher.executeCommand(config, String.format(Constants.DELETE_USER, username))
        }catch(Exception e) {
            String message = String.format(WinRMCommandException.DELETE_WINDOWS_USER_ERROR, username, host.host)
            throw new WinRMCommandException(message, e)
        }
    }
    
    static List<String> listUsers(WindowsHost host) throws WinRMCommandException{
        try {
            WinRMGlobalConnectionConfiguration config = new WinRMGlobalConnectionConfiguration(credentialsId: host.credentialsId,
                context: Jenkins.get(), host: host.host, port: host.port, connectionTimeout: host.connectionTimeout,
                authenticationScheme: host.authenticationScheme, useHttps: host.useHttps)
            
            String result = WinRMCommandLauncher.executeCommand(config, String.format(Constants.LIST_USERS, Constants.USERNAME_PATTERN))
            if(StringUtils.isEmpty(result)) return new ArrayList()
                return result as List
        }catch(Exception e) {
            String message = String.format(WinRMCommandException.LIST_USERS_ERROR_MESSAGE, e.getMessage(), host.host)
            throw new WinRMCommandException(message, e)
        }
    }
}
