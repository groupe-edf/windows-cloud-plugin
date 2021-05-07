package fr.edf.jenkins.plugins.windows.connector

import org.kohsuke.stapler.DataBoundSetter

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import hudson.model.AbstractDescribableImpl
import hudson.slaves.ComputerLauncher

/**
 * 
 * @author Mathieu Delrocq
 *
 */
abstract class WindowsComputerConnector extends AbstractDescribableImpl<WindowsComputerConnector>{

    String jenkinsUrl
    Integer port
    boolean useHttps = Boolean.FALSE
    boolean disableCertificateCheck = Boolean.FALSE
    String credentialsId;
    Integer connectionTimeout
    Integer readTimeout
    Integer agentConnectionTimeout
    Integer maxTries


    public WindowsComputerConnector(String jenkinsUrl, Integer port, Boolean useHttps, Boolean disableCertificateCheck,
    String credentialsId, Integer connectionTimeout, Integer readTimeout, Integer agentConnectionTimeout, Integer maxTries) {
        super()
        this.jenkinsUrl = jenkinsUrl
        this.port = port
        this.useHttps = useHttps
        this.disableCertificateCheck = disableCertificateCheck
        this.credentialsId = credentialsId
        this.connectionTimeout = connectionTimeout
        this.readTimeout = readTimeout
        this.agentConnectionTimeout = agentConnectionTimeout
    }

    @DataBoundSetter
    void setJenkinsUrl(String jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl
    }

    public String getJenkinsUrl() {
        return jenkinsUrl
    }

    public Boolean getUseHttps() {
        return useHttps
    }

    @DataBoundSetter
    void setUseHttps(Boolean useHttps) {
        this.useHttps = useHttps
    }

    public Boolean getDisableCertificateCheck() {
        return disableCertificateCheck
    }

    @DataBoundSetter
    void setDisableCertificateCheck(Boolean disableCertificateCheck) {
        this.disableCertificateCheck = disableCertificateCheck
    }

    public Integer getPort() {
        return port
    }

    @DataBoundSetter
    void setPort(Integer port) {
        this.port = port
    }

    public String getCredentialsId() {
        return credentialsId
    }

    @DataBoundSetter
    void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout
    }

    @DataBoundSetter
    void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout
    }

    public Integer getReadTimeout() {
        return readTimeout
    }

    @DataBoundSetter
    void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout
    }

    public Integer getAgentConnectionTimeout() {
        return agentConnectionTimeout
    }

    @DataBoundSetter
    void setAgentConnectionTimeout(Integer agentConnectionTimeout) {
        this.agentConnectionTimeout = agentConnectionTimeout;
    }

    public Integer getMaxTries() {
        return maxTries
    }

    @DataBoundSetter
    void setMaxTries(Integer maxTries) {
        this.maxTries = maxTries
    }

    /**
     * Build and return the Launcher for a given connector
     * @param host
     * @param user
     * @return Computer Launcher 
     * @throws IOException
     * @throws InterruptedException
     */
    protected abstract ComputerLauncher createLauncher(WindowsHost host, WindowsUser user) throws IOException, InterruptedException

    /**
     * List the usernames of created by windows-cloud-plugin on the given WindowsHost
     * @param host
     * @return list of usernames
     */
    protected abstract List<String> listUsers(WindowsHost host) throws WindowsCommandException;

    /**
     * Remove the given user on the given WindowsHost
     * @param host
     * @param user
     */
    protected abstract void deleteUser(WindowsHost host, String username) throws WindowsCommandException, Exception;
}
