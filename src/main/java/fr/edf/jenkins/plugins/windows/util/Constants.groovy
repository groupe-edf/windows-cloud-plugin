package fr.edf.jenkins.plugins.windows.util;
/**
 * List of constants that represent PowerShell commands
 * @author Abdelkrim ELOTMANI
 *
 */
class Constants {

    static final String EMPTY_LIST_BOX_NAME = "-------"

    static final String EMPTY_LIST_BOX_VALUE = "--------"

    /** windows-%s */
    static final String USERNAME_PATTERN = "windows-%s"

    /** jnlpJars/remoting.jar-%s */
    static final String REMOTING_JAR_URL = "jnlpJars/remoting.jar"

    /** whoami */
    static final String WHOAMI  = "whoami"

    /** Remote Management Users */
    static final String REMOTE_MANAGEMENT_USERS_GROUP = "Remote Management Users"

    /** New-LocalUser \"%s\" -Password \$(\"%s\" | ConvertTo-SecureString -AsPlainText -Force) -FullName \"%s\" -Description \"User automatically created by jenkins\" */
    static final String CREATE_USER = "New-LocalUser \"%s\" -Password \$(\"%s\" | ConvertTo-SecureString -AsPlainText -Force) -FullName \"%s\" -Description \"User automatically created by jenkins\""

    /** Add-LocalGroupMember -Group \"%s\" -Member \"%s\ */
    static final String ADD_USER_TO_GROUP = "Add-LocalGroupMember -Group \"%s\" -Member \"%s\""

    /** Get-Process -IncludeUserName | Where-Object {\$_.UserName -eq \"\$env:COMPUTERNAME\\%s\"} | Stop-Process -Force */
    static final String STOP_USER_PROCESS = "Get-Process -IncludeUserName | Where-Object {\$_.UserName -eq \"\$env:COMPUTERNAME\\%s\"} | Stop-Process -Force"

    /** Remove-LocalUser -Name \"%s\" -Verbose */
    static final String DELETE_USER = "Remove-LocalUser -Name \"%s\" -Verbose"

    /** C:\\Users\\%s\\ */
    static final String WORKDIR_PATTERN = "C:\\Users\\%s\\"

    /** New-Item -Path %s -ItemType 'directory' -Force */
    static final String CREATE_DIR = "New-Item -Path %s -ItemType 'directory' -Force"

    /** \$acl = Get-Acl \"C:\\users\\%s\" \$acl.SetAccessRuleProtection(\$true,\$true) \$acl | Set-Acl \"C:\\users\\%s\" */
    static final String DISABLE_INHERITED_WORKDIR = "\$acl = Get-Acl \"C:\\users\\%s\";" \
                                                    + "\$acl.SetAccessRuleProtection(\$true,\$true);" \
                                                    + "\$acl | Set-Acl \"C:\\users\\%s\""

    /** \$acl = Get-Acl \"C:\\users\\%s\"
     * \$acl.Access | Where-Object {\$_.IdentityReference -notlike \"*Administrators*\" -and \$_.IdentityReference -notlike \"*SYSTEM*\"} | ForEach-Object -Process {\$acl.RemoveAccessRule(\$_)}
     * \$aclDef = \"\$env:COMPUTERNAME\\%s\", \"FullControl\", \"ContainerInherit,ObjectInherit\", \"None\", \"Allow\"
     * \$aclRule = New-Object System.Security.AccessControl.FileSystemAccessRule \$aclDef
     * \$acl.SetAccessRule(\$aclRule)
     * \$acl.SetOwner([System.Security.Principal.NTAccount]\"NT AUTHORITY\\SYSTEM\")
     * \$acl | Set-Acl \"C:\\users\\%s\" */
    static final String GRANT_ACCESS_WORKDIR = "\$acl = Get-Acl \"C:\\users\\%s\";" \
                                                + "\$acl.Access | Where-Object {\$_.IdentityReference -notlike \"*Administrators*\" -and \$_.IdentityReference -notlike \"*SYSTEM*\"} | ForEach-Object -Process {\$acl.RemoveAccessRule(\$_)};" \
                                                + "\$aclDef = \"\$env:COMPUTERNAME\\%s\", \"FullControl\", \"ContainerInherit,ObjectInherit\", \"None\", \"Allow\";" \
                                                + "\$aclRule = New-Object System.Security.AccessControl.FileSystemAccessRule \$aclDef;" \
                                                + "\$acl.SetAccessRule(\$aclRule);" \
                                                + "\$acl.SetOwner([System.Security.Principal.NTAccount]\"NT AUTHORITY\\SYSTEM\");" \
                                                + "\$acl | Set-Acl \"C:\\users\\%s\""

    /** Remove-Item 'C:\\Users\\%s' -Force -Recurse */
    static final String REMOVE_WORKDIR = "Remove-Item 'C:\\Users\\%s' -Force -Recurse"

    /** Check-Path -PathType Container 'C:\\Users\\%s' */
    static final String CHECK_WORKDIR_EXIST = "Test-Path -PathType Container 'C:\\Users\\%s'"

    /** Invoke-RestMethod -Uri %s -OutFile remoting.jar */
    static final String GET_REMOTING_JAR = "Invoke-RestMethod -Uri %s -OutFile remoting.jar"

    /** (Get-LocalUser | Where-Object {\$_.Name -eq %s}).name */
    static final String CHECK_USER_EXIST = "(Get-LocalUser | Where-Object {\$_.Name -eq '%s'}).name"

    /** java -jar remoting.jar -jnlpUrl %scomputer/%s/slave-agent.jnlp -secret %s */
    static final String LAUNCH_JNLP = "java -jar remoting.jar -jnlpUrl %scomputer/%s/slave-agent.jnlp -secret %s"

    /** (Get-LocalUser | Where-Object {\$_.Name -match \"windows-*\"}).name */
    static final String LIST_USERS = "(Get-LocalUser | Where-Object {\$_.Name -match \"windows-*\"}).name"

    /** \\r?\\n|\\r */
    static final String REGEX_NEW_LINE = "\\r?\\n|\\r"
}
