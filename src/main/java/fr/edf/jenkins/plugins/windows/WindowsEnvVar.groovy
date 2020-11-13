package fr.edf.jenkins.plugins.windows

import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

import hudson.Extension
import hudson.model.Describable
import hudson.model.Descriptor
import jenkins.model.Jenkins

class WindowsEnvVar implements Describable<WindowsEnvVar>{
    
    String key
    String value
    
    @DataBoundConstructor
    public WindowsEnvVar(String key, String value) {
        this.key = key
        this.value = value
    }
    
    @DataBoundSetter
     void setKey(String key) {
        this.key = key
    }
    
    @DataBoundSetter
     void setValue(String value) {
        this.value = value
    }

    @Override
     Descriptor<WindowsEnvVar> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(this.getClass())
    }
    
    @Extension
    static class DescriptorImpl extends Descriptor<WindowsEnvVar> {
        /**
         * {@inheritDoc}
         */
        @Override
        String getDisplayName() {
            return Messages.EnvVar_DisplayName()
        }
    }
    
}
