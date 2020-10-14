package fr.edf.jenkins.plugins.windows


import org.junit.Rule
import org.junit.jupiter.api.Test
import org.jvnet.hudson.test.JenkinsRule

import fr.edf.jenkins.plugins.windows.pojos.WindowsPojoBuilder
import spock.lang.Specification

class WindowsCloudTest extends Specification{
    
    @Rule
    JenkinsRule rule = new JenkinsRule()
    
    def "create cloud"(){
        given:
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(WindowsPojoBuilder.buildWindowsHost())
        
        when:
        rule.jenkins.clouds.add(cloud)
        
        then:
        notThrown Exception
        cloud == rule.jenkins.getCloud("cloudTest")
    }
    


}
