package fr.edf.jenkins.plugins.windows.http.connection

import hudson.model.ModelObject

class HttpConnectionConfiguration {
    String host
    String contextPath
    Integer port
    Integer connectionTimeout
    Integer readTimeout
    Boolean useHttps
    Boolean disableCertificateCheck
    ModelObject context
    String credentialsId
}
