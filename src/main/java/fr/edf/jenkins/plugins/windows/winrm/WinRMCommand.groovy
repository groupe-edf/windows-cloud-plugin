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
        return WinRMCommandLauncher.executeCommand(config, Constants.WHOAMI, false, false)
    }

    /**
     * Checks if user exists
     * @param config
     * @param username
     * @return true if the user exists otherwise false
     * @throws Exception
     */
    private static boolean doesUserExist(WinRMConnectionConfiguration config, String username) throws Exception{
        String res = WinRMCommandLauncher.executeCommand(config, String.format(Constants.CHECK_USER_EXIST, username, false, false))
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
        password += "123!"
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
    static WindowsUser createUser(WindowsHost host, WindowsUser user) throws WinRMCommandException, Exception{

        try {
            WinRMGlobalConnectionConfiguration config = new WinRMGlobalConnectionConfiguration(credentialsId: host.credentialsId,
            context: Jenkins.get(), host: host.host, port: host.port, connectionTimeout: host.connectionTimeout,
            authenticationScheme: host.authenticationScheme, useHttps: host.useHttps)

            WinRMCommandLauncher.executeCommand(config, String.format(Constants.CREATE_USER, user.username, user.password.getPlainText(), user.username), false, false)
            if(!doesUserExist(config, user.username)) {
                throw new Exception(String.format("The user %s already exists", user.username))
            }
            WinRMCommandLauncher.executeCommand(config, String.format(Constants.CREATE_DIR, String.format(Constants.WORKDIR_PATTERN, user.username)), false, false)
            WinRMCommandLauncher.executeCommand(config, String.format(Constants.DISABLE_INHERITED_WORKDIR, user.username, user.username), false, false)
            WinRMCommandLauncher.executeCommand(config, String.format(Constants.GRANT_ACCESS_WORKDIR, user.username, user.username, user.username), false, false)
            WinRMCommandLauncher.executeCommand(config, String.format(Constants.ADD_USER_TO_GROUP, Constants.REMOTE_MANAGEMENT_USERS_GROUP, user.username), false, false)
            return user
        } catch(Exception e) {
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
        try {
            WinRMGlobalConnectionConfiguration config = new WinRMGlobalConnectionConfiguration(credentialsId: host.credentialsId,
            context: Jenkins.get(), host: host.host, port: host.port, connectionTimeout: host.connectionTimeout,
            authenticationScheme: host.authenticationScheme, useHttps: host.useHttps)

            WinRMCommandLauncher.executeCommand(config, String.format(Constants.STOP_USER_PROCESS, username), false, false)
            WinRMCommandLauncher.executeCommand(config, String.format(Constants.DELETE_USER, username), false, false)

            if(doesUserExist(config, username)) {
                throw new Exception(String.format("The user %s was not deleted", username))
            }
        }catch(Exception e) {
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

            String result = WinRMCommandLauncher.executeCommand(config, String.format(Constants.LIST_USERS, Constants.USERNAME_PATTERN.substring(0, Constants.USERNAME_PATTERN.lastIndexOf("%"))), false, false)
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
        jenkinsUrl = StringUtils.isNotEmpty(jenkinsUrl) ?: Jenkins.get().getRootUrl()
        if(!jenkinsUrl.endsWith("/")) {
            jenkinsUrl += "/"
        }

        String remotingUrl = jenkinsUrl + Constants.REMOTING_JAR_PATH

        try {
            WinRMUserConnectionConfiguration config = new WinRMUserConnectionConfiguration(username: user.username, password: user.password,
            host: host.host, port: host.port, connectionTimeout: host.connectionTimeout, authenticationScheme: host.authenticationScheme,
            useHttps: host.useHttps)
            WinRMCommandLauncher.executeCommand(config, String.format(Constants.GET_REMOTING_JAR, remotingUrl), false, false)
            WinRMCommandLauncher.executeCommand(config, String.format(Constants.LAUNCH_JNLP, jenkinsUrl, user.username, slaveSecret), true, true)
            return true
        }catch(Exception e) {
            final String message = String.format(WinRMCommandException.JNLP_CONNETION_ERROR, host.host, user.username)
            e.getMessage()
            throw new WinRMCommandException(message, e)
        }
    }
}
