package fr.edf.jenkins.plugins.windows.winrm.connection

import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl

import fr.edf.jenkins.plugins.windows.util.CredentialsUtils
import fr.edf.jenkins.plugins.windows.winrm.client.WinRMTool
import hudson.util.Secret
import jenkins.model.Jenkins
import spock.lang.Specification

class WinRMConnectionFactoryTest extends Specification{

    @Rule
    JenkinsRule rule =  new JenkinsRule()

    def "getWinRMConnection with no parameters returns null"(){

        given:
        WinRMGlobalConnectionConfiguration conf = null

        when:
        WinRMTool tool = WinRMConnectionFactory.getWinRMConnection(null)

        then:
        notThrown Exception
        tool == null
    }

    def"connection for global"(){

        given:
        String host ="host"
        StandardUsernamePasswordCredentials cred = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
                "1",
                "description",
                "username",
                "password")

        WinRMTool tool = Mock()
        GroovySpy(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getConnection(host, cred, _, _, _, _, _, _) >> tool
        }
        GroovyStub(CredentialsUtils, global:true){
            CredentialsUtils.findCredentials(host, _, _) >> cred
        }

        when:
        WinRMTool res = WinRMConnectionFactory.getWinRMConnection(new WinRMGlobalConnectionConfiguration(credentialsId: host,
        context: Jenkins.get(), host: host, port: null, connectionTimeout: null, authenticationScheme: null, useHttps: null, disableCertificateCheck: null))

        then:
        notThrown Exception
        res==tool
    }

    def"connection for user"(){

        given:
        String host ="host"
        WinRMTool tool = Mock()
        GroovySpy(WinRMConnectionFactory, global:true){
            WinRMConnectionFactory.getConnection(host, _, _, _, _, _, _, _) >> tool
        }

        when:
        WinRMTool res = WinRMConnectionFactory.getWinRMConnection(new WinRMUserConnectionConfiguration(username: "username",
        password: Secret.fromString("password"),
        host: host, port: null, connectionTimeout: null, readTimeout: null, authenticationScheme: null, useHttps: null, disableCertificateCheck: null))

        then:
        notThrown Exception
        res==tool
    }
}
