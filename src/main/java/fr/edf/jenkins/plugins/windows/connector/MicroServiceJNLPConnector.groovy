package fr.edf.jenkins.plugins.windows.connector

import static com.cloudbees.plugins.credentials.CredentialsMatchers.anyOf
import static com.cloudbees.plugins.credentials.CredentialsMatchers.instanceOf
import static com.cloudbees.plugins.credentials.domains.URIRequirementBuilder.fromUri

import java.time.Instant
import java.util.logging.Level
import java.util.logging.Logger

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
import fr.edf.jenkins.plugins.windows.util.WindowsCloudUtils
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

    private static final Logger LOGGER = Logger.getLogger(MicroServiceJNLPConnector.class.name)

    private MicroserviceHttpClient client
    private String contextPath

    @DataBoundConstructor
    MicroServiceJNLPConnector(Boolean useHttps, Boolean disableCertificateCheck, Integer port,
    String credentialsId, Integer connectionTimeout, Integer readTimeout,
    Integer agentConnectionTimeout, String jenkinsUrl, String contextPath, Integer maxTries) {
        super(jenkinsUrl, port, useHttps, disableCertificateCheck, credentialsId, connectionTimeout, readTimeout, agentConnectionTimeout, maxTries)
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
        return new MicroServiceJNLPLauncher(host, user, getClient(host))
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> listUsers(WindowsHost host) throws MicroserviceCommandException {
        try {
            LOGGER.log(Level.FINE, "######## Listing user on the host $host.host...")
            ExecutionResult executionResult = getClient(host).listUser();
            if(executionResult.code != 0) {
                throw new MicroserviceCommandException("Command exit status : " + executionResult.code + "\n Error output : " + executionResult.error)
            }
            LOGGER.log(Level.FINEST, "######## $host.host : List user -> Execution Result : \n Code : $executionResult.code \n Output : $executionResult.output \n Error : $executionResult.error")
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
            LOGGER.log(Level.FINE, "######## $host.host -> $username : Deleting user...")
            ExecutionResult executionResult = getClient(host).deleteUser(username)
            if(executionResult.code != 0) {
                throw new MicroserviceCommandException("Command exit status : " + executionResult.code + "\n Error output : " + executionResult.error)
            }
            LOGGER.log(Level.FINEST, "######## $host.host -> $username : Delete user -> Execution Result : \n Code : $executionResult.code \n Output : $executionResult.output \n Error : $executionResult.error")
        } catch(Exception e) {
            String message = String.format(WinRMCommandException.DELETE_WINDOWS_USER_ERROR, username, host.host)
            throw new MicroserviceCommandException(message, e)
        }
    }

    private MicroserviceHttpClient getClient(WindowsHost host) {
        if(this.client == null) {
            this.client = HttpConnectionFactory.getHttpConnection(
                    new HttpConnectionConfiguration(
                    host: host.host,
                    contextPath: contextPath,
                    credentialsId: credentialsId,
                    port: port,
                    connectionTimeout: connectionTimeout,
                    readTimeout: readTimeout,
                    useHttps: useHttps,
                    disableCertificateCheck: disableCertificateCheck,
                    context: Jenkins.get()))
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
                    fromUri(WindowsCloudUtils.getUri(host).toString()).build(),
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
                        host: host,
                        contextPath: contextPath,
                        credentialsId: credentialsId,
                        port: port,
                        connectionTimeout: connectionTimeout,
                        readTimeout: readTimeout,
                        useHttps: useHttps,
                        disableCertificateCheck: disableCertificateCheck,
                        context: item
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

        private static final Logger LOGGER = Logger.getLogger(MicroServiceJNLPLauncher.class.name)

        WindowsHost host
        WindowsUser user
        MicroserviceHttpClient client
        boolean launched

        MicroServiceJNLPLauncher(WindowsHost host, WindowsUser user, MicroserviceHttpClient client) {
            super(true)
            this.host = host
            this.user = user
            this.client = client
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
            ExecutionResult result = null
            try {
                LOGGER.log(Level.FINE, "######## $host.host -> $user.username : Creating user ...")
                result = client.createUser(user)
                LOGGER.log(Level.FINEST, "######## $host.host -> $user.username : Create user -> Execution Result : \n Code : $result.code \n Output : $result.output \n Error : $result.error")
                LOGGER.log(Level.FINE, "######## $host.host -> $user.username : Getting remoting...")
                result = client.getRemoting(user, host.connector.jenkinsUrl)
                LOGGER.log(Level.FINEST, "######## $host.host -> $user.username : Get remoting -> Execution Result : \n Code : $result.code \n Output : $result.output \n Error : $result.error")
                LOGGER.log(Level.FINE, "######## $host.host -> $user.username : Launching Jnlp...")
                result = client.connectJnlp(user, host.connector.jenkinsUrl, computer.getJnlpMac())
                LOGGER.log(Level.FINEST, "######## $host.host -> $user.username : Launch Jnlp -> Execution Result : \n Code : $result.code \n Output : $result.output \n Error : $result.error")
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
                if((Instant.now().toEpochMilli() - currentTimestamp) > host.connector.agentConnectionTimeout.multiply(1000).intValue()) {
                    launched = false
                    String message = toString().format("Connection timeout for the computer %s", computer.name)
                    listener.error(message)
                    throw new InterruptedException(message)
                }
            }
        }
    }
}
