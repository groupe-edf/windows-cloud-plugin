package fr.edf.jenkins.plugins.windows.winrm.connection

import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import io.cloudsoft.winrm4j.winrm.WinRmTool
import spock.lang.Specification

class WinRMConnectionFactoryTest extends Specification{

    @Rule
    JenkinsRule rule =  new JenkinsRule()

    def "getWinRMConnection with no parameters returns null"(){

        when:
        WinRmTool tool = WinRMConnectionFactory.getWinRMConnection(null,null)

        then:
        notThrown Exception
        tool == null
    }
}
