package fr.edf.jenkins.plugins.windows.connector

import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.winrm.WinRMCommandException
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

    /**
     * List the usernames of created by windows-cloud-plugin on the given WindowsHost
     * @param host
     * @return list of usernames
     */
    protected abstract List<String> listUsers(WindowsHost host) throws WinRMCommandException;

    /**
     * Remove the given user on the given WindowsHost
     * @param host
     * @param user
     */
    protected abstract void deleteUser(WindowsHost host, String username) throws WinRMCommandException, Exception;
}
