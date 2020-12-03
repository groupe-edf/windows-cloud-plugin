package fr.edf.jenkins.plugins.windows.winrm.connection

import hudson.model.ModelObject
import hudson.util.Secret
/**
 * Connection configuration
 * @author CHRIS BAHONDA
 *
 */
abstract class WinRMConnectionConfiguration {
    String host
    Integer port
    Integer connectionTimeout
    Integer readTimeout
    String authenticationScheme
    Boolean useHttps
    Boolean disableCertificateCheck
}
/**
 * Global connection configuration
 * @author CHRIS BAHONDA
 *
 */
class WinRMGlobalConnectionConfiguration extends WinRMConnectionConfiguration{
    String credentialsId
    ModelObject context
}
/**
 * User connection configuration
 * @author CHRIS BAHONDA
 *
 */
class WinRMUserConnectionConfiguration extends WinRMConnectionConfiguration{
    String username
    Secret password
}
