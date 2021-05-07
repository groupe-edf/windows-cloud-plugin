package fr.edf.jenkins.plugins.windows.agent

import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import fr.edf.jenkins.plugins.windows.WindowsCloud
import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.connector.WindowsComputerConnector
import fr.edf.jenkins.plugins.windows.pojos.WindowsPojoBuilder
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommand
import spock.lang.Specification

class WindowsAgentTest extends Specification{
    @Rule
    JenkinsRule rule

    def"should create agent"(){

        given:
        WindowsComputerConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        List<WindowsHost> host = WindowsPojoBuilder.buildWindowsHost(connector)
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(host)
        WindowsUser user = WindowsPojoBuilder.buildUser()
        WindowsAgent agent = WindowsPojoBuilder.buildAgent(cloud.name, user, host.get(0), connector)

        when:
        rule.jenkins.get().clouds.add(cloud)
        rule.jenkins.get().addNode(agent)

        then:
        notThrown Exception
        rule.jenkins.get().getNode(agent.name)==agent
    }

    def"should terminate and remove agent"(){
        given:
        WindowsComputerConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        List<WindowsHost> host = WindowsPojoBuilder.buildWindowsHost(connector)
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(host)
        WindowsUser user = WindowsPojoBuilder.buildUser()
        WindowsAgent agent = WindowsPojoBuilder.buildAgent(cloud.name, user, host.get(0), connector)

        GroovyStub(WinRMCommand, global:true){
            WinRMCommand.deleteUser(host.get(0), user) >> "OK"
        }
        when:
        rule.jenkins.get().clouds.add(cloud)
        rule.jenkins.get().addNode(agent)
        assert rule.jenkins.get().getNode(agent.name)==agent
        agent.terminate()

        then:
        notThrown Exception
        rule.jenkins.get().getNode(agent.name)==null
    }

    def"should return node name on windows cloud"(){

        given:
        WindowsComputerConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        List<WindowsHost> host = WindowsPojoBuilder.buildWindowsHost(connector)
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(host)
        WindowsUser user = WindowsPojoBuilder.buildUser()
        WindowsAgent agent = WindowsPojoBuilder.buildAgent(cloud.name, user, host.get(0), connector)

        when:
        rule.jenkins.get().clouds.add(cloud)
        rule.jenkins.get().addNode(agent)
        assert rule.jenkins.get().getNode(agent.name)==agent
        String res = agent.getDisplayName()

        then:
        notThrown Exception
        res == agent.name + " on " + cloud.name
    }

    def"should return the windows cloud of the agent"(){

        given:
        WindowsComputerConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        List<WindowsHost> host = WindowsPojoBuilder.buildWindowsHost(connector)
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(host)
        WindowsUser user = WindowsPojoBuilder.buildUser()
        WindowsAgent agent = WindowsPojoBuilder.buildAgent(cloud.name, user, host.get(0), connector)

        when:
        rule.jenkins.get().clouds.add(cloud)
        rule.jenkins.get().addNode(agent)
        assert rule.jenkins.get().getNode(agent.name)==agent
        WindowsCloud res = agent.getCloud()

        then:
        notThrown Exception
        res==cloud
    }

    def"node name is user is username"(){

        given:
        WindowsComputerConnector connector = WindowsPojoBuilder.buildWinRmConnector(rule)
        List<WindowsHost> host = WindowsPojoBuilder.buildWindowsHost(connector)
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(host)
        WindowsUser user = WindowsPojoBuilder.buildUser()
        WindowsAgent agent = WindowsPojoBuilder.buildAgent(cloud.name, user, host.get(0), connector)

        when:
        rule.jenkins.get().addNode(agent)
        assert rule.jenkins.get().getNode(agent.name)==agent
        String res = agent.displayName

        then:
        notThrown Exception
        res == user.username + " on " + "testCloud"
    }
}
