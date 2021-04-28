package fr.edf.jenkins.plugins.windows.connector


import static com.cloudbees.plugins.credentials.CredentialsMatchers.anyOf
import static com.cloudbees.plugins.credentials.CredentialsMatchers.instanceOf
import static com.cloudbees.plugins.credentials.domains.URIRequirementBuilder.fromUri

import java.time.Instant

import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.http.client.config.AuthSchemes
import org.jenkinsci.Symbol
import org.kohsuke.stapler.AncestorInPath
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.verb.POST

import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardListBoxModel
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.agent.WindowsComputer
import fr.edf.jenkins.plugins.windows.util.FormUtils
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommand
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommandException
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMGlobalConnectionConfiguration
import hudson.Extension
import hudson.model.Descriptor
import hudson.model.Item
import hudson.model.TaskListener
import hudson.security.ACL
import hudson.slaves.ComputerLauncher
import hudson.slaves.JNLPLauncher
import hudson.slaves.SlaveComputer
import hudson.util.FormValidation
import hudson.util.ListBoxModel
import jenkins.model.Jenkins

class WinRmJNLPConnector extends WindowsComputerConnector {

    String authenticationScheme
    Integer maxTries
    Integer commandTimeout

    @DataBoundConstructor
    WinRmJNLPConnector(Boolean useHttps, Boolean disableCertificateCheck, Integer port, String authenticationScheme,
    String credentialsId, Integer maxTries, Integer connectionTimeout, Integer readTimeout,
    Integer agentConnectionTimeout, Integer commandTimeout, String jenkinsUrl) {
        super(jenkinsUrl, port, useHttps, disableCertificateCheck, credentialsId, connectionTimeout, readTimeout, agentConnectionTimeout)
        this.authenticationScheme = authenticationScheme
        this.maxTries = maxTries
        this.commandTimeout = commandTimeout
    }



    public String getAuthenticationScheme() {
        return authenticationScheme
    }

    @DataBoundSetter
    void setAuthenticationScheme(String authenticationScheme) {
        this.authenticationScheme = authenticationScheme
    }

    public Integer getMaxTries() {
        return maxTries
    }

    @DataBoundSetter
    void setMaxTries(Integer maxTries) {
        this.maxTries = maxTries
    }

    public Integer getCommandTimeout() {
        return commandTimeout
    }

    @DataBoundSetter
    void setCommandTimeout(Integer commandTimeout) {
        this.commandTimeout = commandTimeout
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> listUsers(WindowsHost host) throws WinRMCommandException {
        return WinRMCommand.listUsers(host);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deleteUser(WindowsHost host, String username) throws WinRMCommandException, Exception {
        WinRMCommand.deleteUser(host, username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ComputerLauncher createLauncher(WindowsHost host, WindowsUser user) {
        return new WinRmJNLPLauncher(host.host, user, jenkinsUrl, this)
    }

    @Extension @Symbol("winrm")
    static final class DescriptorImpl extends Descriptor<WindowsComputerConnector> {

        /**
         * {@inheritDoc}
         */
        @Override
        String getDisplayName() {
            return "WinRM and JNLP"
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

    private static class WinRmJNLPLauncher extends JNLPLauncher {
        String hostname
        WindowsUser user
        String jenkinsUrl
        WinRmJNLPConnector winrmConnector
        boolean launched

        WinRmJNLPLauncher(String hostname, WindowsUser user, String jenkinsUrl, WinRmJNLPConnector winrmConnector) {
            super(true)
            this.hostname = hostname
            this.user = user
            this.jenkinsUrl = jenkinsUrl
            this.winrmConnector = winrmConnector
        }

        /**
         * {@inheritDoc}
         */
        @Override
        boolean isLaunchSupported() {
            return !launched
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void launch(SlaveComputer computer, TaskListener listener) {
            launched = true
            WindowsComputer windowsComputer = (WindowsComputer) computer
            try {
                WinRMCommand.createUser(hostname, winrmConnector, user)
                WinRMCommand.jnlpConnect(hostname, winrmConnector, user, jenkinsUrl, computer.getJnlpMac())
            }catch(Exception e) {
                launched = false
                String message = String.format("Error while connecting computer %s due to %s ",
                        computer.name, ExceptionUtils.getStackTrace(e))
                listener.error(message)
                throw new InterruptedException(message)
            }

            long currentTimestamp = Instant.now().toEpochMilli()
            while(!windowsComputer.isOnline()) {
                if (windowsComputer == null) {
                    launched = false
                    String message = "Node was deleted, computer is null"
                    listener.error(message)
                    throw new IOException(message)
                }
                if (windowsComputer.isOnline()) {
                    break
                }
                if((Instant.now().toEpochMilli() - currentTimestamp) > winrmConnector.agentConnectionTimeout.multiply(1000).intValue()) {
                    launched = false
                    String message = toString().format("Connection timeout for the computer %s", computer.name)
                    listener.error(message)
                    throw new InterruptedException(message)
                }
            }
        }
    }
}
