# Windows Plugin 

This plugin builds Windows agents to perform builds.

## Requirements
** Make sure you have WinRM and PowerShell enabled **

### PowerShell Configuration
Follow the following steps to set user permmissions
1. (Get-PSSessionConfiguration -Name Microsoft.PowerShell).Permission
2. winrm configSDDL default
3. net localgroup "Remote Management Users" /add **username**