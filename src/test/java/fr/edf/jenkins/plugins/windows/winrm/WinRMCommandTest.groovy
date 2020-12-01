package fr.edf.jenkins.plugins.windows.winrm

import org.apache.http.client.config.AuthSchemes
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.pojos.WindowsPojoBuilder
import fr.edf.jenkins.plugins.windows.util.Constants
import fr.edf.jenkins.plugins.windows.winrm.client.WinRMTool
import fr.edf.jenkins.plugins.windows.winrm.client.output.CommandOutput
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionConfiguration
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionFactory
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMGlobalConnectionConfiguration
import spock.lang.Specification

class WinRMCommandTest extends Specification{

    @Rule
    JenkinsRule rule

    def "checkConnection is working"() {

        given:
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost().get(0)
        WinRMConnectionConfiguration config = new WinRMGlobalConnectionConfiguration(
                host: host,
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
        WindowsHost myHost = WindowsPojoBuilder.buildWindowsHost().get(0)
        WindowsUser user = WinRMCommand.generateUser()

        String shellId = "shell"
        String createUserId = "createUser"
        String checkUserExist = "checkUserExist"
        String addUserToGroup = "addUserToGroup"
        String

        WinRMTool tool = Stub(WinRMTool) {
            openShell() >> shellId
            executePSCommand(String.format(Constants.CREATE_USER, user.username, user.password.getPlainText(), user.username)) >> "createUserId"
            executePSCommand(String.format(Constants.CHECK_USER_EXIST, user.username)) >> "checkUserExist"
            executePSCommand(String.format(Constants.ADD_USER_TO_GROUP, Constants.REMOTE_MANAGEMENT_USERS_GROUP, user.username)) >> "addUserToGroup"
            getCommandOutput(shellId, createUserId) >> new CommandOutput(0, "user created", null)
            getCommandOutput(shellId, checkUserExist) >> new CommandOutput(0, user.username, null)
            getCommandOutput(shellId, addUserToGroup) >> new CommandOutput(0, "user added", null)
            deleteShellRequest(shellId) >> {}
        }
        GroovyStub(WinRMConnectionFactory, global:true) {
            WinRMConnectionFactory.getWinRMConnection(*_) >> tool
        }

        when:
        WindowsUser result = WinRMCommand.createUser(myHost, user)

        then:
        notThrown Exception
        assert result.username == user.username
    }


    def "execute command throws no exception"() {
        given:
        WinRMConnectionConfiguration config = Mock(WinRMConnectionConfiguration)
        WinRMCommandLauncher launcher = new WinRMCommandLauncher(config)
        WinRMTool conn = Stub()
        CommandOutput response = Stub()
        String command = "whoami"
        GroovySpy(WinRMCommandLauncher, global:true){
            _ * launcher.executeCommand(_,_,_) >> response.getOutput()
        }

        when:
        conn.executePSCommand(command)

        then:
        notThrown Exception
    }

    def "createUser throws an exception when no credentials are found"(){

        given:
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost().get(0)
        WindowsUser user = WinRMCommand.generateUser()

        String shellId = "shell"
        String createUserId = "createUser"
        String checkUserExist = "checkUserExist"
        String addUserToGroup = "addUserToGroup"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(String.format(Constants.CREATE_USER, user.username, user.password, user.username)) >> "createUserId"
            executePSCommand(String.format(Constants.CHECK_USER_EXIST, user.username)) >> "checkUserExist"
            executePSCommand(String.format(Constants.ADD_USER_TO_GROUP, Constants.REMOTE_MANAGEMENT_USERS_GROUP, user.username)) >> "addUserToGroup"
            getCommandOutput(shellId, createUserId) >> new CommandOutput(0, "user created", null)
            getCommandOutput(shellId, checkUserExist) >> new CommandOutput(0, user.username, null)
            getCommandOutput(shellId, addUserToGroup) >> new CommandOutput(0, "user added", null)
            deleteShellRequest(shellId) >> {}
        }


        GroovyStub(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> null
        }

        when:
        WindowsUser res = WinRMCommand.createUser(host, user)

        then:
        WinRMCommandException e = thrown()
        e.printStackTrace()
    }


    def "createUser throws an exception when user does not exist"(){

        given:
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost().get(0)
        WindowsUser user = WinRMCommand.generateUser()

        String shellId = "shell"
        String createUserId = "createUser"
        String checkUserExist = "checkUserExist"
        String addUserToGroup = "addUserToGroup"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(String.format(Constants.CREATE_USER, user.username, user.password, user.username)) >> "createUserId"
            executePSCommand(String.format(Constants.CHECK_USER_EXIST, user.username)) >> ""
            executePSCommand(String.format(Constants.ADD_USER_TO_GROUP, Constants.REMOTE_MANAGEMENT_USERS_GROUP, user.username)) >> "addUserToGroup"
            getCommandOutput(shellId, createUserId) >> new CommandOutput(0, "user created", null)
            getCommandOutput(shellId, checkUserExist) >> new CommandOutput(0, user.username, null)
            getCommandOutput(shellId, addUserToGroup) >> new CommandOutput(0, "user added", null)
            deleteShellRequest(shellId) >> {}
        }


        GroovyStub(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> tool
        }

        when:
        WindowsUser res = WinRMCommand.createUser(host, user)

        then:
        WinRMCommandException e = thrown()
        e.getMessage().contains("Unable to create WindowsUser on host")
    }


    def "deleteUser works"(){

        given:
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost().get(0)
        String username = "windows_test_user"
        String shellId = "shell"
        String deleteUserId = "deleteUserId"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(String.format(Constants.DELETE_USER, username)) >> "username"
            getCommandOutput(shellId, deleteUserId) >> new CommandOutput(0, "deleteUserId", null)
            deleteShellRequest(shellId) >> {}
        }

        GroovyStub(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> tool
        }

        when:
        String res = WinRMCommand.deleteUser(host, username)

        then:
        notThrown Exception
        assert res == null
    }


    def "deleteUser returns an exception because user stills exists"(){

        given:
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost().get(0)
        String username = "windows_test_user"
        String shellId = "shell"
        String checkUserExist = "checkUserExist"
        String deleteUserId = "deleteUserId"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(String.format(Constants.DELETE_USER, username)) >> "username"
            executePSCommand(String.format(Constants.CHECK_USER_EXIST, username)) >> username
            getCommandOutput(shellId, deleteUserId) >> new CommandOutput(0, "deleteUserId", null)
            getCommandOutput(shellId, checkUserExist) >> new CommandOutput(0, username, null)
            deleteShellRequest(shellId) >> {}
        }

        GroovyStub(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> null
        }

        when:
        String res = WinRMCommand.deleteUser(host, username)


        then:
        WinRMCommandException e = thrown()
        e.getMessage().contains(String.format(WinRMCommandException.DELETE_WINDOWS_USER_ERROR, username, host.host))
    }



    def "listUsers returns a list"(){

        given:
        String shellId = "shell"
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost().get(0)
        String listUserId = "listUserId"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(String.format(Constants.LIST_USERS,
                    Constants.USERNAME_PATTERN.substring(0, Constants.USERNAME_PATTERN.lastIndexOf("%")))) >> listUserId
            getCommandOutput(shellId, listUserId) >> new CommandOutput(0, "listUserId", null)
            deleteShellRequest(shellId) >> {}
        }

        GroovySpy(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> tool
        }

        when:
        List res = WinRMCommand.listUsers(host)

        then:
        notThrown Exception
        res.size()==1
    }

    def "listUsers throws an exception"(){

        given:
        String shellId = "shell"
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost().get(0)
        String listUserId = "listUserId"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(String.format(Constants.LIST_USERS,
                    Constants.USERNAME_PATTERN.substring(0, Constants.USERNAME_PATTERN.lastIndexOf("%")))) >> {throw new Exception("List unvailable")}
            getCommandOutput(shellId, listUserId) >> new CommandOutput(0, "listUserId", null)
            deleteShellRequest(shellId) >> {}
        }

        GroovySpy(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> tool
        }

        when:
        List res = WinRMCommand.listUsers(host)
        then:
        WinRMCommandException e = thrown()
        e.printStackTrace()
        e.LIST_USERS_NOT_AVAILABLE
    }


    def "jnlp throws no exception"(){

        given:
        String shellId = "shell"
        WindowsUser user = WinRMCommand.generateUser()
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost().get(0)
        String jnlpConnect = "jnlpConnect"
        String slaveSecret = "secret"

        WinRMTool tool = Stub(WinRMTool){
            openShell() >> shellId
            executePSCommand(_,_,_) >> jnlpConnect
            getCommandOutput(shellId, jnlpConnect) >> new CommandOutput(0, "jnlpConnect", null)
            deleteShellRequest(shellId) >> {}
        }

        GroovySpy(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getWinRMConnection(_) >> tool
        }

        when:
        WinRMCommand.jnlpConnect(host, user, null, slaveSecret)

        then:
        notThrown Exception
    }

    def "jnlp does throw an exception"(){

        given:
        WindowsUser user = WinRMCommand.generateUser()
        WindowsHost host = WindowsPojoBuilder.buildWindowsHost().get(0)
        String slaveSecret = "secret"

        when:
        WinRMCommand.jnlpConnect(host, user, null, slaveSecret)

        then:
        WinRMCommandException e = thrown()
        e.getMessage().contains("Unable to connect Windows " + host.host + " with user " + user.username + " to Jenkins via jnlp")
    }
}

