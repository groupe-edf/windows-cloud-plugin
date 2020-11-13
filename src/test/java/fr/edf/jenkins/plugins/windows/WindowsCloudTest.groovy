package fr.edf.jenkins.plugins.windows


import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import fr.edf.jenkins.plugins.windows.pojos.WindowsPojoBuilder
import hudson.model.FreeStyleProject
import hudson.model.Label
import spock.lang.Specification

class WindowsCloudTest extends Specification{
    
    @Rule
    JenkinsRule rule = new JenkinsRule()
    
    def "create cloud"(){
        
        given:
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(WindowsPojoBuilder.buildWindowsHost(), WindowsPojoBuilder.buildConnector(rule))
        
        when:
        rule.jenkins.clouds.add(cloud)
        
        then:
        notThrown Exception
        cloud == rule.jenkins.getCloud("testCloud")
        rule.jenkins.clouds.size()==1
    }
    
    def "call provision method"(){
        
        given:
        WindowsCloud cloud = WindowsPojoBuilder.buildWindowsCloud(WindowsPojoBuilder.buildWindowsHost(), WindowsPojoBuilder.buildConnector(rule))
        rule.jenkins.getCloud("testCloud")
        FreeStyleProject project = rule.createFreeStyleProject("test")
        project.setAssignedLabel(Label.parse("testLabel").getAt(0))
        
        when:
        Boolean isBuilt = project.scheduleBuild2(1)
        
        then:
        notThrown Exception
        isBuilt==true
    }

}
