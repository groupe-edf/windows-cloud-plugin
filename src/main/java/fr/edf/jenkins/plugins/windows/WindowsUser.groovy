package fr.edf.jenkins.plugins.windows

class WindowsUser{

    String username
    String password
    String workdir

    WindowsUser(String username, String password, String workdir) {
        this.username = username
        this.password = password
        this.workdir = workdir
    }

    private WindowsUser() {
    }
}
