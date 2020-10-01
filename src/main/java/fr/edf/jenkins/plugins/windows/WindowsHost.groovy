package fr.edf.jenkins.plugins.windows


import static com.cloudbees.plugins.credentials.domains.URIRequirementBuilder.fromUri

import org.apache.http.client.config.AuthSchemes
import org.kohsuke.stapler.AncestorInPath
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.verb.POST

import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardListBoxModel

import fr.edf.jenkins.plugins.windows.util.FormUtils
import hudson.Extension
import hudson.model.Describable
import hudson.model.Descriptor
import hudson.model.Item
import hudson.security.ACL
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
            return Messages.Host_DefaultName()
        }
        
        /**
         * Set the authentication scheme, the default one used is NTLM
         * @param authenticationScheme
         * @return authentication scheme
         */
        @POST
        ListBoxModel doFillAuthenticationSchemeItems(@QueryParameter String authenticationScheme) {
            ListBoxModel result = new ListBoxModel()
            [AuthSchemes.NTLM, AuthSchemes.BASIC, AuthSchemes.KERBEROS].each {
                result.add(it,it)
            }
            return result
        }


          /**
           * Enter username and password to connect to Windows via WinRm         
           * @param host
           * @param credentialsId
           * @param item
           * @return ListBoxModel of Credentials
           */
        @POST
        ListBoxModel doFillCredentialsIdItems(@QueryParameter String host, @QueryParameter String credentialsId,
                @AncestorInPath Item item) {
            StandardListBoxModel result = new StandardListBoxModel()
            boolean notAdmin = item == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER)
            boolean noCredentials = item != null && !item.hasPermission(Item.EXTENDED_READ) &&
                    !item.hasPermission(CredentialsProvider.USE_ITEM)

            if(notAdmin || noCredentials) {
                return result.includeCurrentValue(credentialsId)
            }
            return result
                    .includeEmptyValue()
                    .includeMatchingAs(ACL.SYSTEM,
                    item ?: Jenkins.get(),
                    StandardCredentials.class,
                    fromUri(FormUtils.getUri(host).toString()).build(),
                    CredentialsMatchers.always())
                    .includeCurrentValue(credentialsId)
        }
    }


//    FormValidation doCheckCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
//        
//        boolean notAdmin = item == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER)
//        boolean noCredentials = item != null && !item.hasPermission(Item.EXTENDED_READ) &&
//                !item.hasPermission(CredentialsProvider.USE_ITEM)
//                
//                if(notAdmin || noCredentials) {
//                    return FormValidation.ok()
//                }
//                
//                if(StringUtils)
//    }
}
