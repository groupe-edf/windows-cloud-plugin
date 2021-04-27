package fr.edf.jenkins.plugins.windows


import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang.StringUtils
import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

import fr.edf.jenkins.plugins.windows.connector.WindowsComputerConnector
import fr.edf.jenkins.plugins.windows.util.Constants
import hudson.Extension
import hudson.model.Describable
import hudson.model.Descriptor
import hudson.model.Label
import hudson.model.labels.LabelAtom
import hudson.util.Secret
import jenkins.model.Jenkins
/**
 * WinRm conncetion configuration for a Windows host
 * @author CHRIS BAHONDA
 *
 */
class WindowsHost implements Describable<WindowsHost> {

    String host
    Integer maxUsers
    Boolean disable = Boolean.FALSE
    String label
    List<WindowsEnvVar> envVars = new ArrayList()
    transient Set<LabelAtom> labelSet
    WindowsComputerConnector connector

    @DataBoundConstructor
    WindowsHost(String host, Boolean disable, String label, List<WindowsEnvVar> envVars, WindowsComputerConnector connector) {
        this.host = host
        this.maxUsers = maxUsers
        this.disable = disable
        this.label = label
        this.envVars = envVars
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

    String getLabel() {
        return label
    }

    @DataBoundSetter
    void setLabel(String label) {
        this.label = label
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
     * Randomly generate Windows user
     * @return a new Windows user
     */
    @Restricted(NoExternalUse)
    static WindowsUser generateUser() {
        String username = String.format(Constants.USERNAME_PATTERN, RandomStringUtils.random(10, true, true).toLowerCase())
        String password = RandomStringUtils.random(15, true, true)
        password += "!"
        String workdir = String.format(Constants.WORKDIR_PATTERN, username)
        return new WindowsUser(username: username, password: Secret.fromString(password), workdir: workdir)
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
    }
}
