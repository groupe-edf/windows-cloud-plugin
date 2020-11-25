package fr.edf.jenkins.plugins.windows.slave

import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level
import java.util.logging.Logger

import org.jenkinsci.plugins.durabletask.executors.OnceRetentionStrategy
import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse

import fr.edf.jenkins.plugins.windows.WindowsCloud
import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.cause.WindowsOfflineCause
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommand
import hudson.Extension
import hudson.model.Computer
import hudson.model.TaskListener
import hudson.model.Slave.SlaveDescriptor
import hudson.slaves.AbstractCloudSlave
import hudson.slaves.Cloud
import hudson.slaves.ComputerLauncher
import hudson.slaves.NodeProperty
import hudson.slaves.RetentionStrategy
import jenkins.model.Jenkins
/**
 * Information about Windows node
 * @author CHRIS BAHONDA
 *
 */
class WindowsSlave extends AbstractCloudSlave {
    private static final Logger LOGGER = Logger.getLogger(WindowsSlave.class.name)

    final String cloudId
    final WindowsHost host
    AtomicBoolean acceptingTasks = new AtomicBoolean(true)

    WindowsSlave(String cloud, String label, WindowsUser user, WindowsHost host, ComputerLauncher launcher,
    Integer idleMinutes, List <? extends NodeProperty<?>> nodeProperties){
        super(
        user.username,
        user.workdir,
        launcher
        )
        this.cloudId = cloud
        this.host = host
        setNumExecutors(1)
        setMode(hudson.model.Node.Mode.EXCLUSIVE)
        setLabelString(label)
        setNodeProperties(nodeProperties)
        setRetentionStrategy(buildRetentionStrategy(idleMinutes))
    }

    /**
     * Returns the retention strategy used for this slave
     * @param idleMinutes
     * @return
     */
    private static RetentionStrategy buildRetentionStrategy(Integer idleMinutes) {
        return new OnceRetentionStrategy(idleMinutes.intValue())
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isAcceptingTasks() {
        return acceptingTasks == null || acceptingTasks.get()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getDisplayName() {
        if(cloudId!=null) {
            return getNodeName() + " on " + cloudId
        }
        return getNodeName()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    WindowsComputer createComputer() {
        return WindowsComputerFactory.createInstance(this)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Restricted(NoExternalUse)
    void _terminate(TaskListener listener) throws IOException, InterruptedException {
        try {
            final Computer computer = toComputer()
            if (computer != null) {
                computer.disconnect(new WindowsOfflineCause())
                LOGGER.log(Level.FINE, "Disconnected computer for node {0}.", name)
            }
        } catch (Exception e) {
            String message = String.format("Cannot disconnect computer for node %s", name)
            LOGGER.log(Level.SEVERE, message, e)
            listener.error(message)
        }
        try {
            WinRMCommand.deleteUser(this.host, this.name)
        } catch (Exception e) {
            String message = String.format("Failed to remove user %s on Windows %s due to : %s", this.name, this.host.host, e.message)
            LOGGER.log(Level.SEVERE, message, e)
            listener.fatalError(message)
        }
    }

    /**
     * Retrieves the cloud attached to the WindowsSlave
     * @return WindowsCloud
     */
    WindowsCloud getCloud() {
        if(cloudId == null) return null
        final Cloud cloud = Jenkins.get().getCloud(cloudId)

        if(cloudId==null) {
            throw new RuntimeException("Failed to retrieve Cloud " + cloudId)
        }

        if(!(cloud instanceof WindowsCloud)) {
            throw new RuntimeException(cloudId + " is not a WindowsCloud, it is a " + cloud.getClass().toString())
        }

        return (WindowsCloud) cloud
    }

    @Extension
    static final class WindowsSlaveDescriptor extends SlaveDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        boolean isInstantiable() {
            return false
        }

        /**
         * {@inheritDoc}
         */
        @Override
        String getDisplayName() {
            return "Windows Agent"
        }
    }
}