package fr.edf.jenkins.plugins.windows.winrm

class WinRMCommandException extends Exception{

    static final String CREATE_WINDOWS_USER_ERROR = "Unable to create WindowsUser on host %s"


    WinRMCommandException(String message, Throwable cause){
        super(message, cause)
    }

    WinRMCommandException(String message){
        super(message)
    }

    private WinRMCommandException() {
    }
}
