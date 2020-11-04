package fr.edf.jenkins.plugins.windows.cause

import hudson.slaves.OfflineCause

/**
 * Puts offline a Windows machine
 * @author CHRIS BAHONDA
 *
 */
class WindowsOfflineCause extends OfflineCause{

    @Override
    String toString() {
        // TODO Auto-generated method stub
        return "Remove Windows user"
    }
}
