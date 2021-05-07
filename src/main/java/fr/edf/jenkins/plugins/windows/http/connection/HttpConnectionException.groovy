package fr.edf.jenkins.plugins.windows.http.connection

class HttpConnectionException extends Exception {

    HttpConnectionException(String message, Throwable cause) {
        super(message, cause)
    }

    HttpConnectionException(String message) {
        super(message)
    }

    private HttpConnectionException(Throwable cause) {}
    private HttpConnectionException() {}
}
