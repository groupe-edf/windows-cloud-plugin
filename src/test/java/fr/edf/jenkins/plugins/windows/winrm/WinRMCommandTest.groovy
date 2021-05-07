package fr.edf.jenkins.plugins.windows.winrm

import org.apache.http.client.config.AuthSchemes
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.connector.WinRmJNLPConnector
import fr.edf.jenkins.plugins.windows.pojos.WindowsPojoBuilder
import fr.edf.jenkins.plugins.windows.util.Constants
import fr.edf.jenkins.plugins.windows.winrm.client.WinRMTool
import fr.edf.jenkins.plugins.windows.winrm.client.output.CommandOutput
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionConfiguration
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionFactory
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMGlobalConnectionConfiguration
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMUserConnectionConfiguration
import spock.lang.Specification

class WinRMCommandTest extends Specification{

    @Rule
    JenkinsRule rule

    def "checkConnection is working"() {

        given:
        WinRMConnectionConfiguration config = new WinRMGlobalConnectionConfiguration(
                host: "localhost",
                port: 5986,
                credentialsId: "test",
                authenticationScheme: AuthSchemes.NTLM,
                useHttps: true,
                context: null,
                connectionTimeout: 1)

        String shellId = "shell"
        String commandId = "command"
        CommandOutput output = new CommandOutput(0, "test", null)

        WinRMTool tool = Stub(WinRMTool) {
            openShell() >> shellId
            executePSCommand(Constants.WHOAMI) >> commandId
            getCommandOutput(shellId, commandId) >> output
            deleteShellRequest(shellId) >> {}
        }

        GroovyStub(WinRMConnectionFactory, global:true)
        WinRMConnectionFactory.getWinRMConnection(config) >> tool

        when:
        String result = WinRMCommand.checkConnection(config)

        then:
        notThrown Exception
        assert result == "test"
    }


    def "createUser throws no exception "(){

        given:
        WinRmJNLPConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost(connector).get(0)
        WindowsUser user = WinRMCommand.generateUser()

        String shellId = "shell"
        String createUserId = "createUser"
        String checkUserExist = "checkUserExist"
        String addUserToGroup = "addUserToGroup"

        WinRMTool tool = Stub(WinRMTool) {
            openShell() >> shellId
            executePSCommand(String.format(Constants.CREATE_USER, user.username, user.password.getPlainText(), user.username)) >> createUserId
            executePSCommand(String.format(Constants.CHECK_USER_EXIST, user.username)) >> checkUserExist
            executePSCommand(String.format(Constants.ADD_USER_TO_GROUP, Constants.REMOTE_MANAGEMENT_USERS_GROUP, user.username)) >> addUserToGroup
            getCommandOutput(shellId, createUserId) >> new CommandOutput(0, "user created", null)
            getCommandOutput(shellId, checkUserExist) >> new CommandOutput(0, user.username, null)
            getCommandOutput(shellId, addUserToGroup) >> new CommandOutput(0, "user added", null)
            deleteShellRequest(shellId) >> {}
        }
        GroovyStub(WinRMConnectionFactory, global:true) {
            WinRMConnectionFactory.getWinRMConnection(*_) >> tool
        }

        when:
        WindowsUser result = WinRMCommand.createUser(connector.getConnectionConfig(host.host), user, connector.commandTimeout)

        then:
        notThrown Exception
        assert result.username == user.username
    }

    def "createUser throws an exception when user does not exist"(){

        given:
        WinRmJNLPConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost(connector).get(0)
        WindowsUser user = WinRMCommand.generateUser()


        String shellId = "shell"
        String createUserId = "createUser"
        String checkUserExist = "checkUserExist"
        String addUserToGroup = "addUserToGroup"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(String.format(Constants.CREATE_USER, user.username, user.password, user.username)) >> createUserId
            executePSCommand(String.format(Constants.CHECK_USER_EXIST, user.username)) >> checkUserExist
            executePSCommand(String.format(Constants.ADD_USER_TO_GROUP, Constants.REMOTE_MANAGEMENT_USERS_GROUP, user.username)) >> addUserToGroup
            getCommandOutput(shellId, createUserId) >> new CommandOutput(0, "user created", null)
            getCommandOutput(shellId, checkUserExist) >> new CommandOutput(0, "", null)
            getCommandOutput(shellId, addUserToGroup) >> new CommandOutput(0, "user added", null)
            deleteShellRequest(shellId) >> {}
        }


        GroovyStub(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> tool
        }

        when:
        WindowsUser res = WinRMCommand.createUser(connector.getConnectionConfig(host.host), user, connector.commandTimeout)

        then:
        WinRMCommandException e = thrown()
        e.getMessage().contains(String.format(WinRMCommandException.CREATE_WINDOWS_USER_ERROR, host.host))
        e.getCause().getMessage().contains(String.format(WinRMCommandException.USER_DOES_NOT_EXIST, user.username))
    }


    def "deleteUser works"(){

        given:
        WinRmJNLPConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost(connector).get(0)
        String username = "windows_test_user"
        String shellId = "shell"
        String deleteUserId = "deleteUserId"
        String checkUserExist = "checkUserExist"
        String stopProcessId = "stopProcessId"
        String removeWorkdirId = "removeWorkdirId"
        String checkWorkdirExist = "checkWorkdirExistId"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(String.format(Constants.DELETE_USER, username)) >> deleteUserId
            executePSCommand(String.format(Constants.CHECK_USER_EXIST, username)) >> checkUserExist
            executePSCommand(String.format(Constants.STOP_USER_PROCESS, username)) >> stopProcessId
            executePSCommand(String.format(Constants.REMOVE_WORKDIR, username)) >> removeWorkdirId
            executePSCommand(String.format(Constants.CHECK_WORKDIR_EXIST, username)) >> checkWorkdirExist
            getCommandOutput(shellId, deleteUserId) >> new CommandOutput(0, "user is deleted", null)
            getCommandOutput(shellId, checkUserExist) >> new CommandOutput(0, "", null)
            getCommandOutput(shellId, checkWorkdirExist) >> new CommandOutput(0, "False", null)
            getCommandOutput(shellId, stopProcessId) >> new CommandOutput(0, "the process has been stopped", null)
            getCommandOutput(shellId, removeWorkdirId) >> new CommandOutput(0, "the workdir has been removed", null)
            deleteShellRequest(shellId) >> {}
        }

        GroovyStub(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> tool
        }

        when:
        String res = WinRMCommand.deleteUser(connector.getConnectionConfig(host.host), username, connector.commandTimeout)

        then:
        notThrown Exception
        assert res == null
    }


    def "deleteUser returns an exception because user stills exists"(){

        given:
        WinRmJNLPConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost(connector).get(0)
        String username = "windows_test_user"
        String shellId = "shell"
        String checkUserExist = "checkUserExist"
        String deleteUserId = "deleteUserId"
        String stopProcessId = "stopProcessId"
        String removeWorkdirId = "removeWorkdirId"
        String checkWorkdirExist = "checkWorkdirExistId"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(String.format(Constants.DELETE_USER, username)) >> deleteUserId
            executePSCommand(String.format(Constants.CHECK_USER_EXIST, username)) >> checkUserExist
            executePSCommand(String.format(Constants.STOP_USER_PROCESS, username)) >> stopProcessId
            executePSCommand(String.format(Constants.REMOVE_WORKDIR, username)) >> removeWorkdirId
            executePSCommand(String.format(Constants.CHECK_WORKDIR_EXIST, username)) >> checkWorkdirExist
            getCommandOutput(shellId, deleteUserId) >> new CommandOutput(0, "the user was not deleted", null)
            getCommandOutput(shellId, checkUserExist) >> new CommandOutput(0, username, null)
            getCommandOutput(shellId, checkWorkdirExist) >> new CommandOutput(0, "False", null)
            getCommandOutput(shellId, stopProcessId) >> new CommandOutput(0, "the process has been stopped", null)
            getCommandOutput(shellId, removeWorkdirId) >> new CommandOutput(0, "the workdir has been removed", null)
            deleteShellRequest(shellId) >> {}
        }

        GroovyStub(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> tool
        }

        when:
        String res = WinRMCommand.deleteUser(connector.getConnectionConfig(host.host), username, connector.commandTimeout)


        then:
        WinRMCommandException e = thrown()
        e.getMessage().contains(String.format(WinRMCommandException.DELETE_WINDOWS_USER_ERROR, username, host.host))
        e.getCause().getMessage().contains(String.format(WinRMCommandException.USER_STILL_EXISTS, username))
    }

    def "deleteUser returns an exception because workdir stills exists"(){

        given:
        WinRmJNLPConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost(connector).get(0)
        String username = "windows_test_user"
        String shellId = "shell"
        String checkUserExist = "checkUserExist"
        String deleteUserId = "deleteUserId"
        String stopProcessId = "stopProcessId"
        String removeWorkdirId = "removeWorkdirId"
        String checkWorkdirExist = "checkWorkdirExistId"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(String.format(Constants.DELETE_USER, username)) >> deleteUserId
            executePSCommand(String.format(Constants.CHECK_USER_EXIST, username)) >> checkUserExist
            executePSCommand(String.format(Constants.STOP_USER_PROCESS, username)) >> stopProcessId
            executePSCommand(String.format(Constants.REMOVE_WORKDIR, username)) >> removeWorkdirId
            executePSCommand(String.format(Constants.CHECK_WORKDIR_EXIST, username)) >> checkWorkdirExist
            getCommandOutput(shellId, deleteUserId) >> new CommandOutput(0, "the user was not deleted", null)
            getCommandOutput(shellId, checkUserExist) >> new CommandOutput(0, "", null)
            getCommandOutput(shellId, checkWorkdirExist) >> new CommandOutput(0, "True", null)
            getCommandOutput(shellId, stopProcessId) >> new CommandOutput(0, "the process has been stopped", null)
            getCommandOutput(shellId, removeWorkdirId) >> new CommandOutput(0, "the workdir has been removed", null)
            deleteShellRequest(shellId) >> {}
        }

        GroovyStub(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> tool
        }

        when:
        String res = WinRMCommand.deleteUser(connector.getConnectionConfig(host.host), username, connector.commandTimeout)


        then:
        WinRMCommandException e = thrown()
        e.getMessage().contains(String.format(WinRMCommandException.DELETE_WINDOWS_USER_ERROR, username, host.host))
        e.getCause().getMessage().contains(String.format(WinRMCommandException.WORKDIR_STILL_EXISTS, username))
    }



    def "listUsers returns a list"(){

        given:
        WinRmJNLPConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost(connector).get(0)
        String shellId = "shell"
        String listUserId = "listUserId"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(String.format(Constants.LIST_USERS,
                    Constants.USERNAME_PATTERN.substring(0, Constants.USERNAME_PATTERN.lastIndexOf("%")))) >> listUserId
            getCommandOutput(shellId, listUserId) >> new CommandOutput(0, "user\r\nusers1", null)
            deleteShellRequest(shellId) >> {}
        }

        GroovySpy(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> tool
        }

        when:
        List res = WinRMCommand.listUsers(connector.getConnectionConfig(host.host), connector.commandTimeout)

        then:
        notThrown Exception
        res.size()==2
    }


    def "jnlp throws no exception"(){

        given:
        String shellId = "shell"
        WindowsUser user = WinRMCommand.generateUser()
        WinRmJNLPConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost(connector).get(0)
        String jnlpConnect = "jnlpConnect"
        String agentSecret = "secret"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(_,_,_) >> jnlpConnect
            getCommandOutput(shellId, jnlpConnect) >> new CommandOutput(0, "jnlp connected", null)
            deleteShellRequest(shellId) >> {}
        }

        GroovySpy(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> tool
        }

        WinRMUserConnectionConfiguration userConfig = new WinRMUserConnectionConfiguration(
                username: user.username,
                password: user.password,
                host: host.host,
                port: connector.port,
                authenticationScheme: connector.authenticationScheme,
                useHttps: connector.useHttps,
                disableCertificateCheck: connector.disableCertificateCheck,
                connectionTimeout: connector.connectionTimeout,
                readTimeout: connector.readTimeout)

        when:
        WinRMCommand.jnlpConnect(userConfig, connector.jenkinsUrl, agentSecret, connector.commandTimeout)

        then:
        notThrown Exception
    }

    def "jnlp does throw an exception"(){

        given:
        WindowsUser user = WinRMCommand.generateUser()
        WinRmJNLPConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost(connector).get(0)
        String agentSecret = "secret"

        WinRMUserConnectionConfiguration userConfig = new WinRMUserConnectionConfiguration(
                username: user.username,
                password: user.password,
                host: host.host,
                port: connector.port,
                authenticationScheme: connector.authenticationScheme,
                useHttps: connector.useHttps,
                disableCertificateCheck: connector.disableCertificateCheck,
                connectionTimeout: connector.connectionTimeout,
                readTimeout: connector.readTimeout)

        when:
        WinRMCommand.jnlpConnect(userConfig, connector.jenkinsUrl, agentSecret, connector.commandTimeout)

        then:
        WinRMCommandException e = thrown()
        e.getMessage().contains("Unable to connect Windows " + host.host + " with user " + user.username + " to Jenkins via jnlp")
    }
}

