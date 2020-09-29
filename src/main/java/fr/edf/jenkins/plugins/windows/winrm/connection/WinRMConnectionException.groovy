package fr.edf.jenkins.plugins.windows.winrm.connection

class WinRMConnectionException extends Exception {

    WinRMConnectionException(String message, Throwable cause) {
        super(message, cause)
    }

    WinRMConnectionException(String message) {
        super(message)
    }

    private WinRMConnectionException(Throwable cause) {}
    private WinRMConnectionException() {}
}
