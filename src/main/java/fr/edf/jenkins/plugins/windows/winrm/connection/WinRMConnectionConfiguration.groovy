package fr.edf.jenkins.plugins.windows.winrm.connection

import hudson.model.ModelObject

abstract class WinRMConnectionConfiguration {
    String host
    Integer port
    String authenticationScheme
    Boolean useHttps
}

class WinRMGlobalConnectionConfiguration extends WinRMConnectionConfiguration{
    String credentialsId
    ModelObject context
}

class WinRMUserConnectionConfiguration extends WinRMConnectionConfiguration{
    String username
    String password
}
