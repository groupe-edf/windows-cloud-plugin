package fr.edf.jenkins.plugins.windows.winrm

import org.antlr.v4.runtime.misc.NotNull
import org.apache.cxf.common.i18n.Exception
import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse

import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionConfiguration
import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionFactory
import io.cloudsoft.winrm4j.client.WinRmClientContext
import io.cloudsoft.winrm4j.winrm.WinRmTool
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse

class WinRMCommandLauncher {

    @Restricted(NoExternalUse)
    protected static String executeCommand(@NotNull WinRMConnectionConfiguration connectionConfiguration, @NotNull String command) throws Exception{

        WinRmTool connection = null
        WinRmToolResponse response = null

        WinRmClientContext winRMContext = WinRmClientContext.newInstance()
        connection = WinRMConnectionFactory.getWinRMConnection(connectionConfiguration, winRMContext)
        response = connection.executePs(command)
        winRMContext.shutdown()
        if(response.getStatusCode()==0) {
            return response.getStdOut()
        } else {
            throw new Exception(response.getStdErr())
        }
    }
}
