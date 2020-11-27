package fr.edf.jenkins.plugins.windows.pojos

import org.apache.http.client.config.AuthSchemes
import org.jvnet.hudson.test.JenkinsRule

import fr.edf.jenkins.plugins.windows.WindowsCloud
import fr.edf.jenkins.plugins.windows.WindowsEnvVar
import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.connector.WindowsComputerConnector
import fr.edf.jenkins.plugins.windows.connector.WindowsComputerJNLPConnector
import fr.edf.jenkins.plugins.windows.slave.WindowsSlave
import hudson.util.Secret

class WindowsPojoBuilder {

    static List<WindowsHost> buildWindowsHost(){
        WindowsHost host = new WindowsHost("localhost",
                "1",
                5986,
                AuthSchemes.NTLM,
                5,
                Boolean.FALSE,
                60,
                60,
                5,
                "testLabel",
                Boolean.FALSE,
                buildEnvVars())
        List<WindowsHost> hostList = new ArrayList()
        hostList.add(host)
        return hostList
    }

    static WindowsUser buildUser() {
        return new WindowsUser("admin", Secret.fromString("admin"), "/Users/admin")
    }

    static WindowsCloud buildWindowsCloud(List<WindowsHost> host, WindowsComputerConnector connector) {
        return new WindowsCloud("testCloud", host, connector, new Integer(1))
    }
    
    static WindowsComputerConnector buildConnector(JenkinsRule jenkinsRule) {
        return new WindowsComputerJNLPConnector(jenkinsRule.getURL().toString())
    }
    
    static WindowsSlave buildSlave(String cloudId, WindowsUser user, WindowsHost host, WindowsComputerConnector connector) {
        return new WindowsSlave(cloudId, "testLabel", user, host, connector.createLauncher(host, user), new Integer(1), Collections.EMPTY_LIST)
    }
    
    static List<WindowsEnvVar> buildEnvVars(){
        List<WindowsEnvVar> envVars = new ArrayList()
        envVars.add(new WindowsEnvVar("TEST1", "test1"))
        envVars.add(new WindowsEnvVar("TEST2", "test2"))
        return envVars
    }
}