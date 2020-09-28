package fr.edf.jenkins.plugins.windows

import hudson.model.Describable

class WindowsEnvVar implements Describable<WindowsEnvVar> {
    
    String key
    String value
    
    WindowsEnvVar(String Key, String value){
        this.key = key
        this.value = value
    }
}
