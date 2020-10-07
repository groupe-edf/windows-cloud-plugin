package fr.edf.jenkins.plugins.windows.util

import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import spock.lang.Specification

class FormUtilsTest extends Specification{
    
    @Rule
    JenkinsRule rule = new JenkinsRule()
    
    def "getUri does not throw an exception"(){
        given:
        String url = rule.getURL().toString()
        
        when:
        URI uri = FormUtils.getUri(url)
        
        then:
        notThrown Exception
        uri != null
    }
    
    def "getUri returns URI"(){
        given:
        String url = "http://localhost"
        
        when:
        URI uri = FormUtils.getUri(url)
        
        then:
        notThrown Exception
        uri.getHost() == "localhost"
    }

    
}
