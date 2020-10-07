package fr.edf.jenkins.plugins.windows.util

import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.domains.HostnameSpecification
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl

import jenkins.model.Jenkins
import spock.lang.Specification

class CredentialsUtilsTest extends Specification{


    @Rule
    JenkinsRule rule = new JenkinsRule()

    def "findCredentials returns id"() {
        
        given:
        Domain domain = new Domain("domain", "testing domain", Arrays.asList(new HostnameSpecification("example.org", null)))
        StandardUsernamePasswordCredentials cred1 = new UsernamePasswordCredentialsImpl( CredentialsScope.SYSTEM,
                "test1",
                null,
                "username",
                "pass"
                )
        StandardUsernamePasswordCredentials cred2 = new UsernamePasswordCredentialsImpl( CredentialsScope.SYSTEM,
                "test2",
                null,
                "username",
                "pass"
                )
        CredentialsProvider.lookupStores(rule.jenkins).iterator().next().addDomain(domain, cred1)
        CredentialsProvider.lookupStores(rule.jenkins).iterator().next().addDomain(domain, cred2)

        when:
        StandardCredentials cred = CredentialsUtils.findCredentials("example.org", "test1", Jenkins.get())

        then:
        assert cred != null
        assert cred instanceof StandardUsernamePasswordCredentials
        assert cred.id == "test1"
    }

//    def "findCredentials throws an exception when id does not exist"(){
//        given:
//        Domain domain = new Domain("domain", "testing domain", Arrays.asList(new HostnameSpecification("host.org", null)))
//        StandardUsernamePasswordCredentials cred1 = new UsernamePasswordCredentialsImpl( CredentialsScope.SYSTEM,
//                "test1",
//                null,
//                "username",
//                "pass"
//                )
//        StandardUsernamePasswordCredentials cred2 = new UsernamePasswordCredentialsImpl( CredentialsScope.SYSTEM,
//                "test2",
//                null,
//                "username",
//                "pass"
//                )
//        CredentialsProvider.lookupStores(rule.jenkins).iterator().next().addDomain(domain, cred1)
//        CredentialsProvider.lookupStores(rule.jenkins).iterator().next().addDomain(domain, cred2)
//
//        when:
//        StandardCredentials cred = CredentialsUtils.findCredentials("host.org", "test", Jenkins.get())
//
//        then:
//        IllegalArgumentException except = thrown()
//    }
}
