package fr.edf.jenkins.plugins.windows.winrm

class WinRMCommandException extends Exception{

    static final String CREATE_WINDOWS_USER_ERROR = "Unable to create WindowsUser on host %s"
    static final String DELETE_WINDOWS_USER_ERROR = "Unable to delete WindowsUser %s on host %s"
    static final String LIST_USERS_ERROR_MESSAGE = "Unable to get the following users %s on windowsHost %s"

    WinRMCommandException(String message, Throwable cause){
        super(message, cause)
    }

    WinRMCommandException(String message){
        super(message)
    }

    private WinRMCommandException() {}
    
}
