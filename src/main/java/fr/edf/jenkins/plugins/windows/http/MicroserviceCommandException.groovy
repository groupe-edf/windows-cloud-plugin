package fr.edf.jenkins.plugins.windows.http

import fr.edf.jenkins.plugins.windows.connector.WindowsCommandException

class MicroserviceCommandException extends WindowsCommandException {

    MicroserviceCommandException(String message, Throwable cause) {
        super(message, cause)
    }

    MicroserviceCommandException(String message) {
        super(message)
    }

    private MicroserviceCommandException(Throwable cause) {}
    private MicroserviceCommandException() {}
}
