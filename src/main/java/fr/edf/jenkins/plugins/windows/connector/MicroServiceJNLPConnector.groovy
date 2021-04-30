package fr.edf.jenkins.plugins.windows.connector

import static com.cloudbees.plugins.credentials.CredentialsMatchers.anyOf
import static com.cloudbees.plugins.credentials.CredentialsMatchers.instanceOf
import static com.cloudbees.plugins.credentials.domains.URIRequirementBuilder.fromUri

import java.time.Instant

import org.apache.commons.lang.exception.ExceptionUtils
import org.jenkinsci.Symbol
import org.jenkinsci.plugins.plaincredentials.StringCredentials
import org.kohsuke.stapler.AncestorInPath
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.verb.POST

import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardListBoxModel

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.agent.WindowsComputer
import fr.edf.jenkins.plugins.windows.http.MicroserviceHttpClient
import fr.edf.jenkins.plugins.windows.http.connection.HttpConnectionConfiguration
import fr.edf.jenkins.plugins.windows.http.connection.HttpConnectionFactory
import fr.edf.jenkins.plugins.windows.util.FormUtils
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommandException
import hudson.Extension
import hudson.model.Descriptor
import hudson.model.Item
import hudson.model.TaskListener
import hudson.security.ACL
import hudson.slaves.ComputerLauncher
import hudson.slaves.JNLPLauncher
import hudson.slaves.SlaveComputer
import hudson.util.ListBoxModel
import jenkins.model.Jenkins

class MicroServiceJNLPConnector extends WindowsComputerConnector {

    private MicroserviceHttpClient client
    private String contextPath

    @DataBoundConstructor
    MicroServiceJNLPConnector(Boolean useHttps, Boolean disableCertificateCheck, Integer port,
    String credentialsId, Integer connectionTimeout, Integer readTimeout,
    Integer agentConnectionTimeout, String jenkinsUrl, contextPath) {
        super(jenkinsUrl, port, useHttps, disableCertificateCheck, credentialsId, connectionTimeout, readTimeout, agentConnectionTimeout)
        this.contextPath = contextPath
    }

    String getContextPath() {
        return contextPath
    }

    @DataBoundSetter
    void setContextPath(String contextPath) {
        this.contextPath = contextPath
    }

    @Override
    protected ComputerLauncher createLauncher(WindowsHost host, WindowsUser user) {
        return new MicroServiceJNLPLauncher(host, user, jenkinsUrl)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> listUsers(WindowsHost host) throws WinRMCommandException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deleteUser(WindowsHost host, String username) throws WinRMCommandException, Exception {
    }

    private MicroserviceHttpClient getClient(WindowsHost host) {
        if(this.client == null) {
            this.client = HttpConnectionFactory.getHttpConnection(new HttpConnectionConfiguration(host, contextPath, credentialsId, port, connectionTimeout, readTimeout, useHttps, disableCertificateCheck))
        }
        return client
    }

    @Extension @Symbol("microservice")
    static final class DescriptorImpl extends Descriptor<WindowsComputerConnector> {

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
                    anyOf(instanceOf(StringCredentials)))
                    .includeCurrentValue(credentialsId)
        }

        /**
         * {@inheritDoc}
         */
        @Override
        String getDisplayName() {
            return "Microservice and JNLP"
        }
    }

    private static class MicroServiceJNLPLauncher extends JNLPLauncher {
        WindowsHost host
        WindowsUser user
        String jenkinsUrl
        MicroServiceJNLPConnector microServiceJNLPConnector
        boolean launched

        MicroServiceJNLPLauncher(WindowsHost host, WindowsUser user, String jenkinsUrl) {
            super(true)
            this.host = host
            this.user = user
            this.jenkinsUrl = jenkinsUrl
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
                if((Instant.now().toEpochMilli() - currentTimestamp) > microServiceJNLPConnector.agentConnectionTimeout.multiply(1000).intValue()) {
                    launched = false
                    String message = toString().format("Connection timeout for the computer %s", computer.name)
                    listener.error(message)
                    throw new InterruptedException(message)
                }
            }
        }
    }
}
