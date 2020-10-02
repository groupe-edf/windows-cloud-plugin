package fr.edf.jenkins.plugins.windows.winrm

import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse

import fr.edf.jenkins.plugins.windows.util.Constants
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMGlobalConnectionConfiguration

class WinRMCommand {
    
    
    @Restricted(NoExternalUse)
    static String checkConnection(WinRMGlobalConnectionConfiguration config) {
        return WinRMCommandLauncher.executeCommand(config, Constants.WHOAMI)
    }
}
