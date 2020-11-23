# Windows Plugin 

This plugin builds Windows agents to perform builds.

## Requirements

** Make sure you have WinRM and PowerShell enabled **

### WinRM Configuration

Follow the following steps to set permissions in the PowerShell console of the host
1. (Get-PSSessionConfiguration -Name Microsoft.PowerShell).Permission
2. winrm configSDDL default 
3. When the pop up window shows, do:
    * Click on add
    * Click on locations
    * Select your computer
    * Click on advanced
    * Click on find now 
    * Select Remote management users 
    * Allow only Execute for user permission
4. Restart-Service winrm