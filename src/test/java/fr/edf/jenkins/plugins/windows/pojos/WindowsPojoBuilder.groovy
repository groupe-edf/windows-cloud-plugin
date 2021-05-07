package fr.edf.jenkins.plugins.windows.pojos

import org.apache.http.client.config.AuthSchemes
import org.jvnet.hudson.test.JenkinsRule

import fr.edf.jenkins.plugins.windows.WindowsCloud
import fr.edf.jenkins.plugins.windows.WindowsEnvVar
import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.agent.WindowsAgent
import fr.edf.jenkins.plugins.windows.connector.WindowsComputerConnector
import fr.edf.jenkins.plugins.windows.connector.WinRmJNLPConnector
import fr.edf.jenkins.plugins.windows.winrm.client.WinRMTool
import hudson.util.Secret

class WindowsPojoBuilder {

    static List<WindowsHost> buildWindowsHost(WindowsComputerConnector connector){
        WindowsHost host = new WindowsHost(
                "localhost",
                false,
                "testLabel",
                buildEnvVars(),
                connector)
        List<WindowsHost> hostList = new ArrayList()
        hostList.add(host)
        return hostList
    }

    static WindowsUser buildUser() {
        return new WindowsUser("admin", Secret.fromString("admin"), "/Users/admin")
    }

    static WindowsCloud buildWindowsCloud(List<WindowsHost> host) {
        return new WindowsCloud("testCloud", host, Integer.valueOf(1))
    }

    static WinRmJNLPConnector buildWinRmConnector(JenkinsRule jenkinsRule) {
        return new WinRmJNLPConnector(
                Boolean.FALSE,
                Boolean.FALSE,
                5986,
                AuthSchemes.NTLM,
                "1",
                5,
                60,
                60,
                60,
                60,
                jenkinsRule.getURL().toString())
    }

    static WindowsAgent buildAgent(String cloudId, WindowsUser user, WindowsHost host, WindowsComputerConnector connector) {
        return new WindowsAgent(cloudId, "testLabel", user, host, connector.createLauncher(host, user), Integer.valueOf(1), Collections.EMPTY_LIST)
    }

    static List<WindowsEnvVar> buildEnvVars(){
        List<WindowsEnvVar> envVars = new ArrayList()
        envVars.add(new WindowsEnvVar("TEST1", "test1"))
        envVars.add(new WindowsEnvVar("TEST2", "test2"))
        return envVars
    }

    static buildWinRMTool() {
        return new WinRMTool(
                "127.0.0.1",
                5986,
                "username",
                Secret.fromString("password"),
                AuthSchemes.NTLM,
                true,
                true,
                15,
                15)
    }
}