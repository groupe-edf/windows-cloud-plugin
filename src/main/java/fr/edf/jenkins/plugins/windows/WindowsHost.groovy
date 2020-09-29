package fr.edf.jenkins.plugins.windows

import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

import hudson.Extension
import hudson.model.Describable
import hudson.model.Descriptor
import jenkins.model.Jenkins

class WindowsHost implements Describable<WindowsHost> {

    String name
    String host
    Integer port
    String authenticationScheme
    Boolean useHttps

    @DataBoundConstructor
    WindowsHost(String name) {
        this.name = name
    }

    String getName() {
        return name
    }

    String getHost() {
        return host
    }

    @DataBoundSetter
    void setHost(String host) {
        this.host = host
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
            return "Windows Host"
        }
    }
}
