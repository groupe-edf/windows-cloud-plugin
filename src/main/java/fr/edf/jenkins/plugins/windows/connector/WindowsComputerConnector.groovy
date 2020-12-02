package fr.edf.jenkins.plugins.windows.connector

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import hudson.model.AbstractDescribableImpl
import hudson.slaves.ComputerLauncher
abstract class WindowsComputerConnector extends AbstractDescribableImpl<WindowsComputerConnector>{

    /**
     * Build and return the Launcher for a given connector
     * @param host
     * @param user
     * @return Computer Launcher 
     * @throws IOException
     * @throws InterruptedException
     */
    protected abstract ComputerLauncher createLauncher(WindowsHost host, WindowsUser user) throws IOException, InterruptedException
}
