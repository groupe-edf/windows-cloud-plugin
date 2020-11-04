package fr.edf.jenkins.plugins.windows.connector

import java.time.Instant

import org.apache.commons.lang.exception.ExceptionUtils
import org.jenkinsci.Symbol
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.slave.WindowsComputer
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommand
import hudson.Extension
import hudson.model.Descriptor
import hudson.model.TaskListener
import hudson.slaves.ComputerLauncher
import hudson.slaves.JNLPLauncher
import hudson.slaves.SlaveComputer

class WindowsComputerJNLPConnector extends WindowsComputerConnector{

    private String JenkinsUrl

    @DataBoundConstructor
    WindowsComputerJNLPConnector(String jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl
    }

    @DataBoundSetter
    void setJenkinsUrl(String jenkinsUrl){
        this.jenkinsUrl = jenkinsUrl
    }

    public String getJenkinsUrl() {
        return jenkinsUrl
    }

    @Extension @Symbol("jnlp")
    static final class DescriptorImpl extends Descriptor<WindowsComputerConnector>{

        /**
         * {@inheritDoc}
         */
        @Override
        String getDisplayName() {
            return "Connect with JNLP"
        }
    }

    @Override
    protected ComputerLauncher createLauncher(WindowsHost host, WindowsUser user) {
        return new WindowsJNLPLauncher(host, user, jenkinsUrl)
    }

    private static class WindowsJNLPLauncher extends JNLPLauncher {
        WindowsHost host
        WindowsUser user
        String jenkinsUrl
        boolean launched

        WindowsJNLPLauncher(WindowsHost host, WindowsUser user, String jenkinsUrl) {
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
                WinRMCommand.createUser(host, user)
                WinRMCommand.jnlpConnect(host, user, jenkinsUrl, computer.getJnlpMac())
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
