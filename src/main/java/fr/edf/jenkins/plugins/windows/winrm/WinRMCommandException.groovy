package fr.edf.jenkins.plugins.windows.winrm

import fr.edf.jenkins.plugins.windows.connector.WindowsCommandException

/**
 * List of command exceptions
 * @author CHRIS BAHONDA
 *
 */
class WinRMCommandException extends WindowsCommandException {

    /**
     * Command exception constructor with both message and cause parameters
     * @param message
     * @param cause
     */
    WinRMCommandException(String message, Throwable cause){
        super(message, cause)
    }
    /**
     * Command exception constructor with message parameter
     * @param message
     */
    WinRMCommandException(String message){
        super(message)
    }
    /**
     * Private constructor, objects cannot be created without parameters
     */
    private WinRMCommandException() {}
}
