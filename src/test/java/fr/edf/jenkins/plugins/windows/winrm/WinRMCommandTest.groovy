package fr.edf.jenkins.plugins.windows.winrm

import org.apache.http.client.config.AuthSchemes
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import fr.edf.jenkins.plugins.windows.util.Constants

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.pojos.WindowsPojoBuilder
import fr.edf.jenkins.plugins.windows.util.CredentialsUtils
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
    // TODO: Fixing launcher

    //    def "createUser throws an exception when no credentials are found"(){
    //
    //        given:
    //        WinRMGlobalConnectionConfiguration config = Mock()
    //        WinRMCommandLauncher launcher = new WinRMCommandLauncher(config)
    //        WindowsHost myHost = Mock()
    //        WindowsUser user = WinRMCommand.generateUser()
    //
    //        GroovySpy(WinRMCommandLauncher, global:true){
    //            1 * launcher.executeCommand(String.format(Constants.CREATE_USER, user.username,
    //                    user.password.getPlainText(), user.username),_,_) >> user.username
    //        }
    //
    //        when:
    //        WinRMCommand.createUser(myHost, user)
    //
    //
    //        then:
    //        WinRMCommandException e = thrown()
    //        e.printStackTrace()
    //    }


//    def "createUser throws an exception when user does not exist"(){
//
//        given:
//        WindowsHost myHost = WindowsPojoBuilder.buildWindowsHost().get(0)
//        WindowsUser user = WinRMCommand.generateUser()
//
//        String shellId = "shell"
//        String createUserId = "createUser"
//        String checkUserExist = "checkUserExist"
//        String addUserToGroup = "addUserToGroup"
//        String
//
//        WinRMTool tool = Stub(WinRMTool) {
//            openShell() >> shellId
//            executePSCommand(String.format(Constants.CREATE_USER, user.username, user.password.getPlainText(), user.username)) >> "createUserId"
//            executePSCommand(String.format(Constants.CHECK_USER_EXIST, user.username)) >> "checkUserExist"
//            executePSCommand(String.format(Constants.ADD_USER_TO_GROUP, Constants.REMOTE_MANAGEMENT_USERS_GROUP, user.username)) >> "addUserToGroup"
//            getCommandOutput(shellId, createUserId) >> new CommandOutput(0, "user created", null)
//            getCommandOutput(shellId, checkUserExist) >> new CommandOutput(0, user.username, null)
//            getCommandOutput(shellId, addUserToGroup) >> new CommandOutput(0, "user added", null)
//            deleteShellRequest(shellId) >> {}
//        }
//        GroovyStub(WinRMConnectionFactory, global:true) {
//            WinRMConnectionFactory.getWinRMConnection(*_) >> tool
//        }
//
//        when:
//        WindowsUser result = WinRMCommand.createUser(myHost, user)
//
//        then:
//        WinRMCommandException e =thrown()
//        e.getMessage().contains("Unable to create WindowsUser on host")
//    }


    //    def "deleteUser works"(){
    //        given:
    //        WinRMGlobalConnectionConfiguration config = Mock()
    //        WinRMCommandLauncher launcher = new WinRMCommandLauncher(config)
    //        String username = "test"
    //        WindowsHost host = Mock()
    //        GroovySpy(WinRMCommandLauncher, global:true){
    //            2 * launcher.executeCommand(_,_,_) >> "OK"
    //        }
    //
    //        when:
    //        WinRMCommand.deleteUser(host, username)
    //
    //        then:
    //        notThrown Exception
    //    }
    //
    //
    //    def "deleteUser returns an exception because user stills exists"(){
    //        given:
    //        WinRMGlobalConnectionConfiguration config = Mock()
    //        WinRMCommandLauncher launcher = new WinRMCommandLauncher(config)
    //        String username = "test"
    //        WindowsHost host = Mock()
    //        GroovySpy(WinRMCommandLauncher, global:true){
    //            1 * launcher.executeCommand(String.format(Constants.DELETE_USER, username),_,_) >> "OK"
    //            1 * launcher.executeCommand(String.format(Constants.CHECK_USER_EXIST, username),_,_) >> username
    //        }
    //        when:
    //        WinRMCommand.deleteUser(host, username)
    //
    //        then:
    //        WinRMCommandException e = thrown()
    //        e.getCause().getMessage().contains("The user " + username + " was not deleted")
    //    }
    //
    //    def "listUsers returns a list"(){
    //        given:
    //        WinRMGlobalConnectionConfiguration config = Mock()
    //        WinRMCommandLauncher launcher = new WinRMCommandLauncher(config)
    //        WindowsHost host = WindowsPojoBuilder.buildWindowsHost().get(0)
    //        GroovySpy(WinRMCommandLauncher, global:true){
    //            1 * launcher.executeCommand(String.format(Constants.LIST_USERS,
    //                    Constants.USERNAME_PATTERN.substring(0, Constants.USERNAME_PATTERN.lastIndexOf("%"))),_,_) >> "use"
    //        }
    //
    //        when:
    //        List res = WinRMCommand.listUsers(host)
    //
    //        then:
    //        notThrown Exception
    //        res.size()==3
    //    }
    //
    //    def "listUsers throws an exception"(){
    //        given:
    //        WinRMGlobalConnectionConfiguration config = Mock()
    //        WinRMCommandLauncher launcher = new WinRMCommandLauncher(config)
    //        WindowsHost host = WindowsPojoBuilder.buildWindowsHost().get(0)
    //        GroovySpy(WinRMCommandLauncher, global:true){
    //            1 * launcher.executeCommand(String.format(Constants.LIST_USERS,
    //                    Constants.USERNAME_PATTERN.substring(0, Constants.USERNAME_PATTERN.lastIndexOf("%"))),_,_) >> {throw new Exception("List unvailable")}
    //        }
    //        when:
    //        List res = WinRMCommand.listUsers(host)
    //
    //        then:
    //        WinRMCommandException e = thrown()
    //        e.printStackTrace()
    //        e.LIST_USERS_NOT_AVAILABLE
    //    }
    //
    //
    //    def "jnlp throws no exception"(){
    //        given:
    //        WinRMGlobalConnectionConfiguration config = Mock()
    //        WinRMCommandLauncher launcher = new WinRMCommandLauncher(config)
    //        WindowsUser user = WinRMCommand.generateUser()
    //        WindowsHost host = Mock()
    //        String slaveSecret = "secret"
    //
    //        GroovySpy(WinRMCommandLauncher, global:true){
    //            _ * launcher.executeCommand(_,_,_) >> "OK"
    //        }
    //
    //        when:
    //        WinRMCommand.jnlpConnect(host, user, null, slaveSecret)
    //
    //        then:
    //        notThrown Exception
    //    }

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

