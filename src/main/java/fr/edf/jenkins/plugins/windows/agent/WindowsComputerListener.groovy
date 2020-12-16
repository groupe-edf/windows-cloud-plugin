package fr.edf.jenkins.plugins.windows.agent

import java.util.logging.Level
import java.util.logging.Logger

import hudson.model.Computer
import hudson.model.TaskListener
import hudson.slaves.ComputerListener
import hudson.slaves.OfflineCause

class WindowsComputerListener extends ComputerListener{

    private static final Logger LOGGER = Logger.getLogger(WindowsComputerListener.class.name)

    /**
     * {@inheritDoc}
     */
    @Override
    void onLaunchFailure(Computer c, TaskListener taskListener) throws IOException, InterruptedException {
        if(c instanceof WindowsComputer) {
            WindowsComputer windowsComputer = (WindowsComputer) c
            LOGGER.log(Level.WARNING, "Windows Agent {0} failed to launch and will be removed", windowsComputer.getName())
            windowsComputer.getNode().terminate()
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onOnline(Computer c, TaskListener listener) {
        if(c instanceof WindowsComputer) {
            WindowsComputer windowsComputer = (WindowsComputer) c
            LOGGER.log(Level.FINE, "Windows Agent {} is now online", windowsComputer.name)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onOffline(Computer c, OfflineCause cause) {
        if(c instanceof WindowsComputer) {
            WindowsComputer windowsComputer = (WindowsComputer) c
            LOGGER.log(Level.FINE, "Windows Agent {} is now offline", windowsComputer.name)
        }
    }
}
