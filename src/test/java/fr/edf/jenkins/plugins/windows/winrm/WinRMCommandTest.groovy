package fr.edf.jenkins.plugins.windows.winrm

import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.pojos.WindowsPojoBuilder
import fr.edf.jenkins.plugins.windows.winrm.client.WinRMTool
import fr.edf.jenkins.plugins.windows.winrm.client.output.CommandOutput
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionConfiguration
import spock.lang.Specification

class WinRMCommandTest extends Specification{

    @Rule
    JenkinsRule rule
// TODO: Fixing launcher for checkConnection
    
//    def "checkConnection is working"(){
//
//        given:
//        WindowsHost host = WindowsPojoBuilder.buildWindowsHost().get(0)
//        WinRMGlobalConnectionConfiguration config = Mock()
//        
//        GroovyStub(CredentialsUtils, global:true){
//            CredentialsUtils.findCredentials(host.host, host.credentialsId, _) >> 
//            new UsernamePasswordCredentialsImpl(CredentialsScope.SYSTEM, host.credentialsId, "description", "username", "password")
//        }
//        WinRMCommandLauncher launcher = new WinRMCommandLauncher(config)
//        
//        GroovySpy(WinRMCommandLauncher, global:true){
//            1 * launcher.executeCommand(_, _, _) >> "OK"
//        }
//
//        when:
//        WinRMCommand.checkConnection(config)
//
//        then:
//        notThrown Exception
//    }


//    def "createUser throws no exception "(){
//
//        given:
//        WindowsHost myHost = WindowsPojoBuilder.buildWindowsHost().get(0)
//        WindowsUser user = WinRMCommand.generateUser()
//        
//        WinRmTool tool = Stub(WinRmTool) {
//            1 * executePs(String.format(Constants.CREATE_USER, user.username, user.password.getPlainText(), user.username)) >> "OK"
//            1 * executePs(String.format(Constants.CHECK_USER_EXIST, user.username)) >> "OK"
//            1 * executePs(String.format(String.format(Constants.DISABLE_INHERITED_WORKDIR, user.username, user.username))) >> "OK"
//            1 * executePs(String.format(Constants.CREATE_DIR, user.username)) >> "OK" >> "OK"
//            1 * executePs(String.format(Constants.GRANT_ACCESS_WORKDIR, user.username, user.username))
//        }
//        GroovyStub(WinRMConnectionFactory, global:true) {
//            5 * WinRMConnectionFactory.getWinRMConnection(*_) >> tool
//        }
//
//        GroovyStub(CredentialsUtils, global:true){
//            CredentialsUtils.findCredentials(myHost.host, myHost.credentialsId, Jenkins.get()) >> 
//            new UsernamePasswordCredentialsImpl(CredentialsScope.SYSTEM, myHost.credentialsId, "description","username", "password")
//        }
//
//        GroovySpy(WinRMCommandLauncher, global:true){
//            1 * WinRMCommandLauncher.executeCommand(_, String.format(Constants.CREATE_USER, user.username,
//                    user.password.getPlainText(), user.username)) >> "OK"
//            //                1 * WinRMCommandLauncher.executeCommand(config, String.format(Constants.CHECK_USER_EXIST, user.username)) >> user.username
//            //                1 * WinRMCommandLauncher.executeCommand(config, String.format(Constants.DISABLE_INHERITED_WORKDIR, user.username, user.username)) >> "OK"
//            //                1 * WinRMCommandLauncher.executeCommand(config, String.format(Constants.CREATE_DIR, user.username)) >> "OK"
//            //                1 * WinRMCommandLauncher.executeCommand(config, String.format(Constants.GRANT_ACCESS_WORKDIR, user.username, user.username)) >> "OK"
//        }
//        
//        
//
//        when:
//        WinRMCommand.createUser(myHost, user)
//
//
//        then:
//        notThrown Exception
//    }

    
    def"execute command throws no exception"(){
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


// TODO: Fixing launcher
    
//    def "createUser throws an exception when user does not exist"(){
//
//        given:
//        WindowsHost myHost = Mock()
//        WinRMGlobalConnectionConfiguration config = Mock()
//        WinRMCommandLauncher launcher = new WinRMCommandLauncher(config)
//        WindowsUser user = WinRMCommand.generateUser()
//
//        GroovySpy(WinRMCommandLauncher, global:true)
//        1* launcher.executeCommand(String.format(Constants.CREATE_USER, user.username,
//                user.password.getPlainText(), user.username),_,_) >> user.username
//        1* launcher.executeCommand(String.format(Constants.CHECK_USER_EXIST, user.username),_,_) >> user.username
//
//        when:
//        WinRMCommand.createUser(myHost, user)
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

