package fr.edf.jenkins.plugins.windows.winrm
/**
 * List of command exceptions
 * @author CHRIS BAHONDA
 *
 */
class WinRMCommandException extends Exception{

    static final String CREATE_WINDOWS_USER_ERROR = "Unable to create WindowsUser on host %s"
    static final String DELETE_WINDOWS_USER_ERROR = "Unable to delete WindowsUser %s on host %s"
    static final String LIST_USERS_ERROR_MESSAGE = "Unable to get the following users %s on windowsHost %s"
    static final String LIST_USERS_NOT_AVAILABLE = "The list is not available"
    static final String JNLP_CONNETION_ERROR = "Unable to connect Windows %s with user %s to Jenkins via jnlp"
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
