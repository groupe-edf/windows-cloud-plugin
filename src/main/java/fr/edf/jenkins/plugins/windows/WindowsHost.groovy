package fr.edf.jenkins.plugins.windows


import static com.cloudbees.plugins.credentials.CredentialsMatchers.anyOf
import static com.cloudbees.plugins.credentials.CredentialsMatchers.instanceOf
import static com.cloudbees.plugins.credentials.domains.URIRequirementBuilder.fromUri

import org.apache.commons.lang.StringUtils
import org.apache.http.client.config.AuthSchemes
import org.kohsuke.stapler.AncestorInPath
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.verb.POST

import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardListBoxModel
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials

import fr.edf.jenkins.plugins.windows.connector.WindowsComputerConnector
import fr.edf.jenkins.plugins.windows.util.FormUtils
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommand
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMGlobalConnectionConfiguration
import hudson.Extension
import hudson.model.Describable
import hudson.model.Descriptor
import hudson.model.Item
import hudson.model.Label
import hudson.model.labels.LabelAtom
import hudson.security.ACL
import hudson.util.FormValidation
import hudson.util.ListBoxModel
import jenkins.model.Jenkins
/**
 * WinRm conncetion configuration for a Windows host
 * @author CHRIS BAHONDA
 *
 */
class WindowsHost implements Describable<WindowsHost> {

    String host
    String credentialsId
    Integer port
    String authenticationScheme
    Integer maxUsers
    Boolean disable = Boolean.FALSE
    Integer connectionTimeout
    Integer readTimeout
    Integer agentConnectionTimeout
    Integer commandTimeout
    Integer maxTries
    String label
    Boolean useHttps = Boolean.FALSE
    Boolean disableCertificateCheck = Boolean.FALSE
    List<WindowsEnvVar> envVars = new ArrayList()
    transient Set<LabelAtom> labelSet
    WindowsComputerConnector connector

    @DataBoundConstructor
    WindowsHost(String host, String credentialsId, Integer port, String authenticationScheme, Integer maxUsers, Boolean disable,
    Integer connectionTimeout, Integer readTimeout, Integer agentConnectionTimeout, Integer commandTimeout, Integer maxTries, String label,
    Boolean useHttps, Boolean disableCertificateCheck, List<WindowsEnvVar> envVars, WindowsComputerConnector connector) {
        this.host = host
        this.credentialsId = credentialsId
        this.port = port
        this.authenticationScheme = authenticationScheme
        this.maxUsers = maxUsers
        this.disable = disable
        this.connectionTimeout = connectionTimeout
        this.readTimeout = readTimeout
        this.agentConnectionTimeout = agentConnectionTimeout
        this.maxTries = maxTries
        this.label = label
        this.useHttps = useHttps
        this.disableCertificateCheck = disableCertificateCheck
        this.envVars = envVars
        this.commandTimeout = commandTimeout
        this.connector = connector;
        labelSet = Label.parse(StringUtils.defaultIfEmpty(label, ""))
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

    Integer getReadTimeout() {
        return readTimeout
    }

    @DataBoundSetter
    void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout
    }

    Integer getAgentConnectionTimeout() {
        return agentConnectionTimeout
    }

    @DataBoundSetter
    void setAgentConnectionTimeout(Integer agentConnectionTimeout) {
        this.agentConnectionTimeout = agentConnectionTimeout
    }

    Integer getCommandTimeout() {
        return commandTimeout
    }

    @DataBoundSetter
    void setCommandTimeout(Integer commandTimeout) {
        this.commandTimeout = commandTimeout
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

    Boolean isUseHttps() {
        return useHttps
    }

    @DataBoundSetter
    void setUseHttps(Boolean useHttps) {
        this.useHttps = useHttps
    }

    Boolean isDisableCertificateCheck() {
        return disableCertificateCheck
    }

    @DataBoundSetter
    void setDisableCertificateCheck(Boolean disableCertificateCheck) {
        this.disableCertificateCheck = disableCertificateCheck
    }

    List<WindowsEnvVar> getEnvVars() {
        return envVars
    }

    @DataBoundSetter
    void setEnvVars(List<WindowsEnvVar> envVars) {
        this.envVars = envVars
    }

    public WindowsComputerConnector getConnector() {
        return connector
    }

    @DataBoundSetter
    public void setConnector(WindowsComputerConnector connector) {
        this.connector = connector
    }

    @Override
    Descriptor<WindowsHost> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(this.getClass())
    }


    Set<LabelAtom> getLabelSet() {
        return Label.parse(StringUtils.defaultIfEmpty(this.label, ""))
    }

    /**
     * Jenkins UI of Windows host
     * @author CHRIS BAHONDA
     *
     */
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
         * List the available authentication schemes for WinRm. However NTLM is the default one
         * @param authenticationScheme
         * @return authentication scheme
         */
        @POST
        ListBoxModel doFillAuthenticationSchemeItems() {
            ListBoxModel result = new ListBoxModel()
            [AuthSchemes.NTLM, AuthSchemes.BASIC].each {
                result.add(it,it)
            }
            return result
        }

        /**
         * List the available credentials
         * @param host
         * @param credentialsId
         * @param item
         * @return CredentialsId
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
                    anyOf(instanceOf(StandardUsernamePasswordCredentials)))
                    .includeCurrentValue(credentialsId)
        }

        /**
         * Checks connection on Windows machine
         * @param host
         * @param port
         * @param credentialsId
         * @param authenticationScheme
         * @param useHttps
         * @param item
         * @return connection success otherwise connection failed
         */
        @POST
        FormValidation doVerifyConnection(@QueryParameter String host, @QueryParameter Integer port,
                @QueryParameter String credentialsId, @QueryParameter String authenticationScheme,
                @QueryParameter Boolean useHttps, @QueryParameter Boolean disableCertificateCheck,
                @QueryParameter Integer connectionTimeout, @QueryParameter Integer readTimeout,
                @AncestorInPath Item item) {

            try {

                Jenkins.get().checkPermission(Jenkins.ADMINISTER)
                String result = WinRMCommand.checkConnection(new WinRMGlobalConnectionConfiguration(
                        credentialsId: credentialsId, context: item, host: host, port: port, authenticationScheme: authenticationScheme,
                        useHttps: useHttps, disableCertificateCheck: disableCertificateCheck,
                        connectionTimeout: connectionTimeout, readTimeout: readTimeout))
                return FormValidation.ok("Connection success : " + (result).toString())
            } catch(Exception e) {
                return FormValidation.error("Connection failed : " + (e.getMessage()).toString())
            }
        }
    }
}
