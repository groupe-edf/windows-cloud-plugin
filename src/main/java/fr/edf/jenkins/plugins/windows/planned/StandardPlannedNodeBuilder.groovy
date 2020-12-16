package fr.edf.jenkins.plugins.windows.planned

import java.util.concurrent.Future
import java.util.logging.Level
import java.util.logging.Logger

import com.google.common.util.concurrent.Futures

import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.agent.WindowsAgent
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommand
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommandException
import hudson.model.Descriptor
import hudson.slaves.ComputerLauncher
import hudson.slaves.NodeProvisioner
import hudson.slaves.NodeProvisioner.PlannedNode

/**
 * The default {@link PlannedNodeBuilder} implementation, in case there are others
 * @author CHRIS BAHONDA
 *
 */
class StandardPlannedNodeBuilder extends PlannedNodeBuilder{

    private static final Logger LOGGER = Logger.getLogger(StandardPlannedNodeBuilder.class.name)

    /**
     * {@inheritDoc}
     */
    @Override
    PlannedNode build() {
        Future f
        WindowsUser user = null
        try {
            user = WinRMCommand.generateUser()
            ComputerLauncher launcher = cloud.connector.createLauncher(windowsHost, user)
            WindowsAgent agent = new WindowsAgent(cloud.name, windowsHost.label, user, windowsHost, launcher, cloud.idleMinutes, nodeProperties)
            f = Futures.immediateFuture(agent)
        } catch (IOException | Descriptor.FormException | WinRMCommandException e) {
            LOGGER.log(Level.SEVERE, e.getMessage())
            LOGGER.log(Level.FINEST, "Exception : ", e)
            f = Futures.immediateFailedFuture(e)
            if (user != null ) {
                WinRMCommand.deleteUser(windowsHost, user.username)
            }
        }
        return new NodeProvisioner.PlannedNode(windowsHost.host, f, numExecutors)
    }
}
