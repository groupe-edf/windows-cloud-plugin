# Windows Plugin

This plugin builds Windows agents to perform builds.

## Requirements
**JNLP must be enabled on Jenkins**


**Make sure you have WinRM configured on your server**

See [Microsoft documentation](https://docs.microsoft.com/en-us/windows/win32/winrm/installation-and-configuration-for-windows-remote-management)

### WinRM Authorization Configuration

In a PowerShell terminal on the server, verify PowerShell permissions :

```
PS C:\Users\admsrv> (Get-PSSessionConfiguration -Name Microsoft.PowerShell).Permission
NT AUTHORITY\INTERACTIVE AccessAllowed, BUILTIN\Administrators AccessAllowed, BUILTIN\Remote Management Users AccessAllo
wed
```

Then launch 

```
PS C:\Users\admsrv> winrm configSDDL default
```

Add permission to "Remote Management User" by following steps :

- Click on Add
    
- Click on Locations
    
- Select your computer
    
<img src="https://zupimages.net/up/20/49/z5sv.png" width="400"/>
    
- Click on Advanced
    
- Click on Find Now 
    
- Select "Remote Management Users" and click on OK

<img src="https://zupimages.net/up/20/49/x2hv.png" width="400"/>

- Allow only Execute for "Remote Management Users"

<img src="https://zupimages.net/up/20/49/eu7o.png" width="300"/>


Restart Winrm with the command :

```
PS C:\Users\admsrv> Restart-Service winrm
```
## Plugin configuration

