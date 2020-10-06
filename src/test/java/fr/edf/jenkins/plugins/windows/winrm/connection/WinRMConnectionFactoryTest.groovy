package fr.edf.jenkins.plugins.windows.winrm.connection

import static org.junit.jupiter.api.Assertions.*

import org.junit.Rule
import org.junit.jupiter.api.Test
import org.jvnet.hudson.test.JenkinsRule

import io.cloudsoft.winrm4j.winrm.WinRmTool

class WinRMConnectionFactoryTest {

    @Rule
    JenkinsRule rule =  new JenkinsRule()
    
    def "getWinRMConnection with no parameters returns null"(){
        when:
        WinRmTool tool = WinRMConnectionFactory.getConnection()
        
        then:
        notThrown Exception
        tool == null
    }

}
