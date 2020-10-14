package fr.edf.jenkins.plugins.windows

import hudson.util.Secret

class WindowsUser{

    String username
    Secret password
    String workdir

    WindowsUser(String username, Secret password, String workdir) {
        this.username = username
        this.password = password
        this.workdir = workdir
    }

    private WindowsUser() {}
}
