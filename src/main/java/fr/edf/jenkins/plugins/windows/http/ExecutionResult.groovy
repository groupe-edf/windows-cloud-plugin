package fr.edf.jenkins.plugins.windows.http

import groovy.transform.ToString

@ToString
class ExecutionResult {

    /** exit code returned by the PowerShell command */
    Integer code

    /** Standard output of the PowerShell command */
    String output

    /** Error output of the PowerShell command */
    String error
}
