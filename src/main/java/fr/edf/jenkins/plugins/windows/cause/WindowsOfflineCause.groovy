package fr.edf.jenkins.plugins.windows.cause

import hudson.slaves.OfflineCause

/**
 * Puts a Windows machine offline 
 * @author CHRIS BAHONDA
 *
 */
class WindowsOfflineCause extends OfflineCause{

    @Override
    String toString() {
        return "Remove Windows user"
    }
}
