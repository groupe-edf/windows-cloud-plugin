package fr.edf.jenkins.plugins.windows.winrm

import org.antlr.v4.runtime.misc.NotNull
import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse

import fr.edf.jenkins.plugins.windows.winrm.client.WinRMException
import fr.edf.jenkins.plugins.windows.winrm.client.WinRMTool
import fr.edf.jenkins.plugins.windows.winrm.client.output.CommandOutput
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionConfiguration
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionFactory

/**
 * Contains method needed to launch commands
 * @author CHRIS BAHONDA
 * @author Mathieu Delrocq
 *
 */
class WinRMCommandLauncher {
    /**
     * Execute the command using the given connection configuration
     * @param connectionConfiguration
     * @param command i.e. the command that need to be launched
     * @return the command output
     * @throws Exception
     */
    @Restricted(NoExternalUse)
    protected static String executeCommand(@NotNull WinRMConnectionConfiguration connectionConfiguration, @NotNull String command) throws Exception{

        WinRMTool winrmTool = null
        CommandOutput output = null
        String shellId = null
        try {
            winrmTool = WinRMConnectionFactory.getWinRMConnection(connectionConfiguration)
            shellId = winrmTool.openShell()
            output = winrmTool.executePSCommand(command)
            if(output.exitStatus==0) {
                winrmTool.deleteShellRequest(shellId)
                return output.output
            } else {
                winrmTool.deleteShellRequest(shellId)
                throw new Exception(output.errorOutput)
            }
        }catch(WinRMException we) {
            if(shellId != null) {
                winrmTool.deleteShellRequest(shellId)
            }
            throw new WinRMCommandException("An unexpected error occured due to exception " + we.getLocalizedMessage(), we)
        }
    }
}
