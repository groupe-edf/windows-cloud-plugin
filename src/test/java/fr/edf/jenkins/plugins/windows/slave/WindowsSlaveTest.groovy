package fr.edf.jenkins.plugins.windows.slave

import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import fr.edf.jenkins.plugins.windows.WindowsCloud
import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.connector.WindowsComputerConnector
import fr.edf.jenkins.plugins.windows.pojos.WindowsPojoBuilder
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommand
import spock.lang.Specification

class WindowsSlaveTest extends Specification{
    @Rule
    JenkinsRule rule

    def"should create slave"(){
        
        given:
        WindowsComputerConnector connector = WindowsPojoBuilder.buildConnector(rule)
        List<WindowsHost> host = WindowsPojoBuilder.buildWindowsHost()
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(host, connector)
        WindowsUser user = WindowsPojoBuilder.buildUser()
        WindowsSlave slave = WindowsPojoBuilder.buildSlave(cloud.name, user, host.get(0), connector)
        
        when:
        rule.jenkins.get().clouds.add(cloud)
        rule.jenkins.get().addNode(slave)
        
        then:
        notThrown Exception
        rule.jenkins.get().getNode(slave.name)==slave
        
    }
    
    def"should terminate and remove slave"(){
        given:
        WindowsComputerConnector connector = WindowsPojoBuilder.buildConnector(rule)
        List<WindowsHost> host = WindowsPojoBuilder.buildWindowsHost()
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(host, connector)
        WindowsUser user = WindowsPojoBuilder.buildUser()
        WindowsSlave slave = WindowsPojoBuilder.buildSlave(cloud.name, user, host.get(0), connector)
        
        GroovyStub(WinRMCommand, global:true){
            WinRMCommand.deleteUser(host.get(0), user) >> "OK"
        }
        when:
        rule.jenkins.get().clouds.add(cloud)
        rule.jenkins.get().addNode(slave)
        assert rule.jenkins.get().getNode(slave.name)==slave
        slave.terminate()
        
        then:
        notThrown Exception
        rule.jenkins.get().getNode(slave.name)==null
    }
    
    def"should return node name on windows cloud"(){
        
        given:
        WindowsComputerConnector connector = WindowsPojoBuilder.buildConnector(rule)
        List<WindowsHost> host = WindowsPojoBuilder.buildWindowsHost()
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(host, connector)
        WindowsUser user = WindowsPojoBuilder.buildUser()
        WindowsSlave slave = WindowsPojoBuilder.buildSlave(cloud.name, user, host.get(0), connector)
        
        when:
        rule.jenkins.get().clouds.add(cloud)
        rule.jenkins.get().addNode(slave)
        assert rule.jenkins.get().getNode(slave.name)==slave
        String res = slave.getDisplayName()
        
        then:
        notThrown Exception
        res == slave.name + " on " + cloud.name
    }
    
    def"should return the windows cloud of the slave"(){
        
        given:
        WindowsComputerConnector connector = WindowsPojoBuilder.buildConnector(rule)
        List<WindowsHost> host = WindowsPojoBuilder.buildWindowsHost()
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(host, connector)
        WindowsUser user = WindowsPojoBuilder.buildUser()
        WindowsSlave slave = WindowsPojoBuilder.buildSlave(cloud.name, user, host.get(0), connector)
        
        when:
        rule.jenkins.get().clouds.add(cloud)
        rule.jenkins.get().addNode(slave)
        assert rule.jenkins.get().getNode(slave.name)==slave
        WindowsCloud res = slave.getCloud()
        
        then:
        notThrown Exception
        res==cloud 
    }
    
    def"node name is user is username"(){
        
        given:
        WindowsComputerConnector connector = WindowsPojoBuilder.buildConnector(rule)
        List<WindowsHost> host = WindowsPojoBuilder.buildWindowsHost()
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(host, connector)
        WindowsUser user = WindowsPojoBuilder.buildUser()
        WindowsSlave slave = WindowsPojoBuilder.buildSlave(cloud.name, user, host.get(0), connector)
        
        when:
        rule.jenkins.get().addNode(slave)
        assert rule.jenkins.get().getNode(slave.name)==slave
        String res = slave.displayName
        
        then:
        notThrown Exception
        res == user.username + " on " + "testCloud"
    }
}
