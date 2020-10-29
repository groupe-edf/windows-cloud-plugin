package fr.edf.jenkins.plugins.windows.winrm.connection
/**
 * Contains connection exception
 * @author CHRIS BAHONDA
 *
 */
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
