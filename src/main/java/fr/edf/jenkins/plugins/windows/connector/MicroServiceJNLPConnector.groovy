package fr.edf.jenkins.plugins.windows.connector

import java.time.Instant

import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.agent.WindowsComputer
import fr.edf.jenkins.plugins.windows.http.MicroserviceHttpClient
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommandException
import hudson.Extension
import hudson.model.Descriptor
import hudson.model.TaskListener
import hudson.slaves.ComputerLauncher
import hudson.slaves.JNLPLauncher
import hudson.slaves.SlaveComputer

class MicroServiceJNLPConnector extends WindowsComputerConnector {

    private MicroserviceHttpClient client

    @DataBoundConstructor
    MicroServiceJNLPConnector(Boolean useHttps, Boolean disableCertificateCheck, Integer port,
    String credentialsId, Integer connectionTimeout, Integer readTimeout,
    Integer agentConnectionTimeout, String jenkinsUrl) {
        super(jenkinsUrl, port, useHttps, disableCertificateCheck, credentialsId, connectionTimeout, readTimeout, agentConnectionTimeout)
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

    @Extension @Symbol("microservice")
    static final class DescriptorImpl extends Descriptor<WindowsComputerConnector>{

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
                if((Instant.now().toEpochMilli() - currentTimestamp) > host.agentConnectionTimeout.multiply(1000).intValue()) {
                    launched = false
                    String message = toString().format("Connection timeout for the computer %s", computer.name)
                    listener.error(message)
                    throw new InterruptedException(message)
                }
            }
        }
    }
}
