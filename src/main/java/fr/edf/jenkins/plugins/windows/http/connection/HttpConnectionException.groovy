package fr.edf.jenkins.plugins.windows.http

class MicroserviceConnectionException {

    MicroserviceConnectionException(String message, Throwable cause) {
        super(message, cause)
    }

    MicroserviceConnectionException(String message) {
        super(message)
    }

    private MicroserviceConnectionException(Throwable cause) {}
    private MicroserviceConnectionException() {}
}
