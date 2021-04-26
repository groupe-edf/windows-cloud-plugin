package fr.edf.jenkins.plugins.windows

import java.util.logging.Level
import java.util.logging.Logger

import org.antlr.v4.runtime.misc.Nullable
import org.apache.commons.collections.CollectionUtils
import org.kohsuke.stapler.DataBoundConstructor

import fr.edf.jenkins.plugins.windows.connector.WindowsComputerConnector
import fr.edf.jenkins.plugins.windows.planned.PlannedNodeBuilderFactory
import fr.edf.jenkins.plugins.windows.provisioning.InProvisioning
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommand
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommandException
import hudson.Extension
import hudson.model.Descriptor
import hudson.model.Label
import hudson.slaves.Cloud
import hudson.slaves.NodeProvisioner.PlannedNode
/**
 * Configuration of a Windows Cloud
 * @author CHRIS BAHONDA
 *
 */
class WindowsCloud extends Cloud {

    private static final Logger LOGGER = Logger.getLogger(WindowsCloud.class.name)

    List<WindowsHost> windowsHosts = new ArrayList()
    Integer idleMinutes

    @DataBoundConstructor
    WindowsCloud(String name, List<WindowsHost> windowsHosts, Integer idleMinutes) {
        super(name)
        this.windowsHosts = windowsHosts
        this.idleMinutes = idleMinutes
    }

    List<WindowsHost> getWindowsHosts() {
        return windowsHosts
    }

    @Nullable
    static getWindowsCloud() {
        return all().get(WindowsCloud)
    }

    /**
     * Gets all MacHost available for the given label
     * @param label
     * @return All MacHosts of this cloud not disabled and matching the given label
     */
    List<WindowsHost> getWindowsHosts(Label label) {
        try {
            return windowsHosts.findAll {
                !it.disable && label.matches((Collection) it.getLabelSet())
            }
        } catch(Exception e) {
            String message = String.format("An error occured when trying to find hosts with label %s", label.toString())
            LOGGER.log(Level.WARNING, message)
            LOGGER.log(Level.FINEST, "Exception : ", e)
            return Collections.emptyList()
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized Collection<PlannedNode> provision(Label label, int excessWorkload) {
        try {
            List<WindowsHost> labelWindowsHosts = getWindowsHosts(label)
            if(CollectionUtils.isEmpty(labelWindowsHosts)) {
                LOGGER.log(Level.WARNING, "No host is configured for the label {0}", label.toString())
                return Collections.emptyList()
            }
            final List<PlannedNode> r = new ArrayList<>()
            Set<String> allInProvisioning = InProvisioning.getAllInProvisioning(label)
            LOGGER.log(Level.FINE, "In provisioning : {0}", allInProvisioning.size())
            int toBeProvisioned = Math.max(0, excessWorkload - allInProvisioning.size())
            LOGGER.log(Level.INFO, "Excess workload after pending Windows agents: {0}", toBeProvisioned)
            if(toBeProvisioned > 0) {
                WindowsHost windowsHost = chooseWindowsHost(labelWindowsHosts)
                r.add(PlannedNodeBuilderFactory.createInstance().cloud(this).host(windowsHost).build())
            }
            return r
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage())
            LOGGER.log(Level.FINEST, "Exception : ", e)
            return Collections.emptyList()
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean canProvision(Label label) {
        boolean canProvision = windowsHosts.find {!it.disable} != null
        if(!canProvision) {
            LOGGER.log(Level.WARNING, "The Windows Cloud {0} is disabled", this.name)
        }
        return canProvision
    }

    /**
     * Returns an available Windows host
     * The host must not be disabled and must be able to create users
     * @param labelWindowsHosts
     * @return WindowsHost
     * @throws Exception
     */
    private WindowsHost chooseWindowsHost(List<WindowsHost> labelWindowsHosts) throws Exception {
        WindowsHost hostChoosen = labelWindowsHosts.find {
            if(it.disable) {
                return false
            }
            int nbTries = 0
            while(true) {
                try {
                    int existingUsers = it.connector.listUsers(it).size()
                    return existingUsers < it.maxUsers
                } catch(WinRMCommandException e) {
                    nbTries ++
                    if(nbTries < it.maxTries) {
                        continue
                    } else {
                        LOGGER.log(Level.INFO, "Disabling Windows Host {0}", it.host)
                        it.disable = true
                        return false
                    }
                }
            }
        }
        if(null == hostChoosen) throw new Exception("Unable to find a Windows host available")
        return hostChoosen
    }

    @Extension
    static class DescriptorImpl extends Descriptor<Cloud> {

        /**
         * {@inheritDoc}
         */
        @Override
        String getDisplayName() {
            return Messages._Cloud_DefaultName()
        }
    }
}
