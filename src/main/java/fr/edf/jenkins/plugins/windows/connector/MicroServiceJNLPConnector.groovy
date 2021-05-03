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
import fr.edf.jenkins.plugins.windows.http.ExecutionResult
import fr.edf.jenkins.plugins.windows.http.MicroserviceCommandException
import fr.edf.jenkins.plugins.windows.http.MicroserviceHttpClient
import fr.edf.jenkins.plugins.windows.http.connection.HttpConnectionConfiguration
import fr.edf.jenkins.plugins.windows.http.connection.HttpConnectionFactory
import fr.edf.jenkins.plugins.windows.util.Constants
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
import hudson.util.FormValidation
import hudson.util.ListBoxModel
import jenkins.model.Jenkins

class MicroServiceJNLPConnector extends WindowsComputerConnector {

    private MicroserviceHttpClient client
    private String contextPath

    @DataBoundConstructor
    MicroServiceJNLPConnector(Boolean useHttps, Boolean disableCertificateCheck, Integer port,
    String credentialsId, Integer connectionTimeout, Integer readTimeout,
    Integer agentConnectionTimeout, String jenkinsUrl, String contextPath) {
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
    protected List<String> listUsers(WindowsHost host) throws MicroserviceCommandException {
        try {
            ExecutionResult executionResult = getClient(host).listUser();
            if(executionResult.code != 0) {
                throw new MicroserviceCommandException("Command exit status : " + executionResult.code + "\n Error output : " + executionResult.error)
            }
            return executionResult.getOutput().split(Constants.REGEX_NEW_LINE) as List
        } catch(Exception e) {
            String message = String.format(WinRMCommandException.LIST_USERS_ERROR_MESSAGE, e.getMessage(), host.host)
            throw new MicroserviceCommandException(message, e)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deleteUser(WindowsHost host, String username) throws WinRMCommandException, Exception {
        try {
            ExecutionResult executionResult = getClient(host).deleteUser(username)
            if(executionResult.code != 0) {
                throw new MicroserviceCommandException("Command exit status : " + executionResult.code + "\n Error output : " + executionResult.error)
            }
        } catch(Exception e) {
            String message = String.format(WinRMCommandException.DELETE_WINDOWS_USER_ERROR, username, host.host)
            throw new MicroserviceCommandException(message, e)
        }
    }

    private MicroserviceHttpClient getClient(WindowsHost host) {
        if(this.client == null) {
            this.client = HttpConnectionFactory.getHttpConnection(
                    new HttpConnectionConfiguration(
                    host,
                    contextPath,
                    credentialsId,
                    port,
                    connectionTimeout,
                    readTimeout,
                    useHttps,
                    disableCertificateCheck,
                    Jenkins.get()))
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
         * Checks connection on Windows machine
         * @param host
         * @param port
         * @param credentialsId
         * @param useHttps
         * @param disableCertificateCheck
         * @param contextPath
         * @param item
         * @return connection success otherwise connection failed
         */
        @POST
        FormValidation doVerifyConnection(@QueryParameter String host, @QueryParameter Integer port,
                @QueryParameter String credentialsId, @QueryParameter Boolean useHttps,
                @QueryParameter Boolean disableCertificateCheck, @QueryParameter String contextPath,
                @QueryParameter Integer connectionTimeout, @QueryParameter Integer readTimeout,
                @AncestorInPath Item item) {
            try {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER)
                ExecutionResult result = HttpConnectionFactory.getHttpConnection(
                        new HttpConnectionConfiguration(
                        host,
                        contextPath,
                        credentialsId,
                        port,
                        connectionTimeout,
                        readTimeout,
                        useHttps,
                        disableCertificateCheck,
                        item
                        )).whoami()
                return FormValidation.ok("Connection success : " + result.output)
            } catch(Exception e) {
                return FormValidation.error("Connection failed : " + (e.getMessage()).toString())
            }
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
                microServiceJNLPConnector.getClient(host).getRemoting(user, jenkinsUrl)
                microServiceJNLPConnector.getClient(host).connectJnlp(user, jenkinsUrl, computer.getJnlpMac())
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
