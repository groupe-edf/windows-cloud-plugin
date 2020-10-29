package fr.edf.jenkins.plugins.windows

import hudson.util.Secret
/**
 * The windows user with a name, password and working directory
 * @author CHRIS BAHONDA
 *
 */
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
