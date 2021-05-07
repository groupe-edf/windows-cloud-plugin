package fr.edf.jenkins.plugins.windows.http

import groovy.transform.ToString

/**
 * Object returned by powershell-daemon apis
 * 
 * @author Mathieu Delrocq
 *
 */
@ToString
class ExecutionResult {

    /** exit code returned by the PowerShell command */
    Integer code

    /** Standard output of the PowerShell command */
    String output

    /** Error output of the PowerShell command */
    String error
}
