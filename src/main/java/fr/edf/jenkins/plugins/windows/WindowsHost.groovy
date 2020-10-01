package fr.edf.jenkins.plugins.windows

import org.apache.http.client.config.AuthSchemes
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.verb.POST

import com.cloudbees.plugins.credentials.common.StandardListBoxModel

import hudson.Extension
import hudson.model.Describable
import hudson.model.Descriptor
import hudson.util.ListBoxModel
import jenkins.model.Jenkins

class WindowsHost implements Describable<WindowsHost> {

    String host
    String credentialsId
    Integer port
    String authenticationScheme
    Integer maxUsers
    Boolean disable
    Integer connectionTimeout
    Integer agentConnectionTimeout
    Integer maxTries
    String label
    Boolean useHttps

    @DataBoundConstructor
    WindowsHost(String host, String credentialsId, Integer port, String authenticationScheme, Integer maxUsers,
    Boolean disable,Integer connectionTimeout, Integer agentConnectionTimeout, Integer maxTries, String label, Boolean useHttps) {
        this.host = host
        this.credentialsId = credentialsId
        this.port = port
        this.authenticationScheme = authenticationScheme
        this.maxUsers = maxUsers
        this.disable = disable
        this.connectionTimeout = connectionTimeout
        this.agentConnectionTimeout = agentConnectionTimeout
        this.maxTries = maxTries
        this.label = label
        this.useHttps = useHttps
    }



    String getHost() {
        return host
    }


    @DataBoundSetter
    void setHost(String host) {
        this.host = host
    }



    String getCredentialsId() {
        return credentialsId
    }


    @DataBoundSetter
    void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId
    }



    Integer getPort() {
        return port
    }


    @DataBoundSetter
    void setPort(Integer port) {
        this.port = port
    }



    String getAuthenticationScheme() {
        return authenticationScheme
    }


    @DataBoundSetter
    void setAuthenticationScheme(String authenticationScheme) {
        this.authenticationScheme = authenticationScheme
    }



    Integer getMaxUsers() {
        return maxUsers
    }


    @DataBoundSetter
    void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers
    }

    

     Boolean getDisable() {
        return disable
    }


    @DataBoundSetter
     void setDisable(Boolean disable) {
        this.disable = disable
    }



    Integer getConnectionTimeout() {
        return connectionTimeout
    }


    @DataBoundSetter
    void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout
    }



    Integer getAgentConnectionTimeout() {
        return agentConnectionTimeout
    }


    @DataBoundSetter
    void setAgentConnectionTimeout(Integer agentConnectionTimeout) {
        this.agentConnectionTimeout = agentConnectionTimeout
    }

    
    
     Integer getMaxTries() {
        return maxTries
    }


    @DataBoundSetter
     void setMaxTries(Integer maxTries) {
        this.maxTries = maxTries
    }



    String getLabel() {
        return label
    }


    @DataBoundSetter
    void setLabel(String label) {
        this.label = label
    }



    Boolean getUseHttps() {
        return useHttps
    }


    @DataBoundSetter
    void setUseHttps(Boolean useHttps) {
        this.useHttps = useHttps
    }



    @Override
    Descriptor<WindowsHost> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(this.getClass())
    }

    @Extension
    static class DescriptorImpl extends Descriptor<WindowsHost> {
        /**
         * {@inheritDoc}
         */
        @Override
        String getDisplayName() {
            return "Windows Host"
        }
        @POST
        ListBoxModel doFillAuthenticationSchemeItems(@QueryParameter String authenticationScheme) {
            ListBoxModel result = new ListBoxModel()
            [AuthSchemes.NTLM, AuthSchemes.BASIC, AuthSchemes.KERBEROS].each { 
                result.add(it,it)
            }
            return result
        }
    }
}
