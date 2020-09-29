package fr.edf.jenkins.plugins.windows

import org.kohsuke.stapler.DataBoundConstructor

import hudson.Extension
import hudson.model.Descriptor
import hudson.model.Label
import hudson.slaves.AbstractCloudImpl
import hudson.slaves.Cloud
import hudson.slaves.NodeProvisioner.PlannedNode

class WindowsCloud extends /*AbstractCloudImpl*/ Cloud {

    List<WindowsHost> windowsHosts

    @DataBoundConstructor
    WindowsCloud(String name, /*String instanceCapStr,*/ List<WindowsHost> windowsHosts) {
        super(name/*, instanceCapStr*/)
        this.windowsHosts = windowsHosts
    }

    List<WindowsHost> getWindowsHosts() {
        return windowsHosts
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized Collection<PlannedNode> provision(Label label, int excessWorkload) {
        return Collections.emptyList()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean canProvision(Label label) {
        return false
    }

    @Extension
    static class DescriptorImpl extends Descriptor<Cloud> {

        /**
         * {@inheritDoc}
         */
        @Override
        String getDisplayName() {
            return "Windows Cloud"
        }
    }
}
