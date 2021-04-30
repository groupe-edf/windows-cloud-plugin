package fr.edf.jenkins.plugins.windows.http.connection

import hudson.model.ModelObject

class HttpConnectionConfiguration {
    final String host
    final String contextPath
    final Integer port
    final Integer connectionTimeout
    final Integer readTimeout
    final Boolean useHttps
    final Boolean disableCertificateCheck
    final ModelObject context
    final String credentialsId

    HttpConnectionConfiguration(String host, String contextPath, String credentialsId, Integer port, Integer connectionTimeout, Integer readTimeout, Boolean useHttps, Boolean disableCertificateCheck) {
        this.host = host
        this.contextPath = contextPath
        this.credentialsId = credentialsId
        this.port = port
        this.connectionTimeout = connectionTimeout
        this.readTimeout = readTimeout
        this.useHttps = useHttps
        this.disableCertificateCheck = disableCertificateCheck
    }
}
