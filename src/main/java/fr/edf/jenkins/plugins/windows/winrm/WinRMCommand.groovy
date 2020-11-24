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
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMUserConnectionConfiguration
import hudson.util.Secret
import jenkins.model.Jenkins
/**
 * List of all methods to execute winrm commands
 * @author CHRIS BAHONDA
 *
 */
class WinRMCommand {

    /**
     * Checks whether the user is connected or not
     * @param config
     * @return which user is connected
     */
    @Restricted(NoExternalUse)
    static String checkConnection(WinRMGlobalConnectionConfiguration config) {
        WinRMCommandLauncher launcher = new WinRMCommandLauncher(config)
        return launcher.executeCommand(Constants.WHOAMI, false, false)
    }

    /**
     * Checks if user exists
     * @param config
     * @param username
     * @return true if the user exists otherwise false
     * @throws Exception
     */
    private static boolean doesUserExist(WinRMCommandLauncher launcher, String username, boolean keepAlive) throws Exception{
        String res = launcher.executeCommand(String.format(Constants.CHECK_USER_EXIST, username), false, keepAlive)
        return res.trim() == username
    }

    /**
     * Randomly generate Windows user
     * @return a new Windows user
     */
    @Restricted(NoExternalUse)
    static WindowsUser generateUser() {
        String username = String.format(Constants.USERNAME_PATTERN, RandomStringUtils.random(10, true, true).toLowerCase())
        String password = RandomStringUtils.random(15, true, true)
        password += "!"
        String workdir = String.format(Constants.WORKDIR_PATTERN, username)
        return new WindowsUser(username: username, password: Secret.fromString(password), workdir: workdir)
    }

    /**
     * Create a user 
     * @param host
     * @param user
     * @return
     * @throws WinRMCommandException
     * @throws Exception
     */
    @Restricted(NoExternalUse)
    static WindowsUser createUser(WindowsHost host, WindowsUser user) throws WinRMCommandException {
        WinRMCommandLauncher launcher = null
        try {
            WinRMGlobalConnectionConfiguration config = new WinRMGlobalConnectionConfiguration(credentialsId: host.credentialsId,
            context: Jenkins.get(), host: host.host, port: host.port, connectionTimeout: host.connectionTimeout,
            authenticationScheme: host.authenticationScheme, useHttps: host.useHttps)
            launcher = new WinRMCommandLauncher(config)

            launcher.executeCommand(String.format(Constants.CREATE_USER, user.username, user.password.getPlainText(), user.username), false, true)
            if(!doesUserExist(launcher, user.username, true)) {
                throw new Exception(String.format("The user %s does not exist after creation", user.username))
            }
            //              Windows Create the user directory and set ACL when the user connect
            //            launcher.executeCommand(String.format(Constants.CREATE_DIR, String.format(Constants.WORKDIR_PATTERN, user.username)), false, true)
            //            launcher.executeCommand(String.format(Constants.DISABLE_INHERITED_WORKDIR, user.username, user.username), false, true)
            //            launcher.executeCommand(String.format(Constants.GRANT_ACCESS_WORKDIR, user.username, user.username, user.username), false, true)

            launcher.executeCommand(String.format(Constants.ADD_USER_TO_GROUP, Constants.REMOTE_MANAGEMENT_USERS_GROUP, user.username), false, false)
            return user
        } catch(Exception e) {
            if(launcher != null && launcher.shellId != null) launcher.closeShell()
            final String message = String.format(WinRMCommandException.CREATE_WINDOWS_USER_ERROR, host.host)
            throw new WinRMCommandException(message, e)
        }
    }

    /**
     * Delete the given user
     * @param host
     * @param username
     * @throws WinRMCommandException
     * @throws Exception
     */
    static void deleteUser(WindowsHost host, String username) throws WinRMCommandException, Exception{
        WinRMCommandLauncher launcher = null
        try {
            WinRMGlobalConnectionConfiguration config = new WinRMGlobalConnectionConfiguration(credentialsId: host.credentialsId,
            context: Jenkins.get(), host: host.host, port: host.port, connectionTimeout: host.connectionTimeout,
            authenticationScheme: host.authenticationScheme, useHttps: host.useHttps)
            launcher = new WinRMCommandLauncher(config)

            launcher.executeCommand(String.format(Constants.STOP_USER_PROCESS, username), false, true)
            launcher.executeCommand(String.format(Constants.DELETE_USER, username), false, true)

            if(doesUserExist(launcher, username, false)) {
                throw new Exception(String.format("The user %s was not deleted", username))
            }
        }catch(Exception e) {
            if(launcher?.shellId) launcher.closeShell()
            String message = String.format(WinRMCommandException.DELETE_WINDOWS_USER_ERROR, username, host.host)
            throw new WinRMCommandException(message, e)
        }
    }

    /**
     * List all users for the given host
     * @param host
     * @return a list of users
     * @throws WinRMCommandException
     */
    static List<String> listUsers(WindowsHost host) throws WinRMCommandException{
        try {
            WinRMGlobalConnectionConfiguration config = new WinRMGlobalConnectionConfiguration(credentialsId: host.credentialsId,
            context: Jenkins.get(), host: host.host, port: host.port, connectionTimeout: host.connectionTimeout,
            authenticationScheme: host.authenticationScheme, useHttps: host.useHttps)
            WinRMCommandLauncher launcher = new WinRMCommandLauncher(config)

            String result = launcher.executeCommand(Constants.LIST_USERS, false, false)
            if(StringUtils.isEmpty(result)) return new ArrayList()
            return result as List
        }catch(Exception e) {
            String message = String.format(WinRMCommandException.LIST_USERS_ERROR_MESSAGE, e.getMessage(), host.host)
            throw new WinRMCommandException(message, e)
        }
    }

    /**
     * Allows Windows users to connect via jnlp
     * @param host
     * @param user
     * @param jenkinsUrl
     * @param slaveSecret
     * @return true if connection succeeds
     * @throws WinRMCommandException
     * @throws Exception
     */
    static boolean jnlpConnect(WindowsHost host, WindowsUser user, String jenkinsUrl, String slaveSecret) throws WinRMCommandException, Exception{
        jenkinsUrl = jenkinsUrl ?: Jenkins.get().getRootUrl()
        if(!jenkinsUrl.endsWith("/")) {
            jenkinsUrl += "/"
        }

        String remotingUrl = jenkinsUrl + Constants.REMOTING_JAR_URL

        WinRMCommandLauncher launcher = null
        try {
            WinRMUserConnectionConfiguration config = new WinRMUserConnectionConfiguration(username: user.username, password: user.password,
            host: host.host, port: host.port, connectionTimeout: host.connectionTimeout, authenticationScheme: host.authenticationScheme,
            useHttps: host.useHttps)
            launcher = new WinRMCommandLauncher(config)
            launcher.executeCommand(String.format(Constants.GET_REMOTING_JAR, remotingUrl), false, true)
            launcher.executeCommand(String.format(Constants.LAUNCH_JNLP, jenkinsUrl, user.username, slaveSecret), true, true)
            return true
        }catch(Exception e) {
            if(launcher?.shellId) launcher.closeShell()
            final String message = String.format(WinRMCommandException.JNLP_CONNETION_ERROR, host.host, user.username)
            throw new WinRMCommandException(message, e)
        }
    }
}
