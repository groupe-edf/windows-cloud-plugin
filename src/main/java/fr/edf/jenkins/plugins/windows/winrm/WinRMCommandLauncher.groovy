package fr.edf.jenkins.plugins.windows.winrm

import java.util.logging.Logger

import fr.edf.jenkins.plugins.windows.winrm.connection.WinRMConnectionConfiguration
import io.cloudsoft.winrm4j.winrm.WinRmTool.Builder

class WinRMCommandLauncher {
    private static final Logger LOGGER = Logger.getLogger(WinRMCommandLauncher.name)
    final static String UTF8 = "UTF-8"
    
    
    protected static String executeCommand(WinRMConnectionConfiguration connectionConfiguration, boolean ignoreError, String command) throws Exception {
                     Builder connection = null
                     try {
                         
                         
                     }catch(Exception e) {
                         
                     }
    }
}
