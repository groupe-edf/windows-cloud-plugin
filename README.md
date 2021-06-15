[<img src="https://i.pinimg.com/originals/bc/00/a8/bc00a8bd0a4be6cd29680d02c70f0539.png" width="100" align="right"/>](https://github.com/groupe-edf)

# Windows Cloud Plugin
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Fwindows-cloud-plugin%2Fmaster)](https://ci.jenkins.io/job/Plugins/job/windows-cloud-plugin/job/master/)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/windows-cloud.svg?color=blue)](https://plugins.jenkins.io/windows-cloud)
[![Coverage Status](https://coveralls.io/repos/github/groupe-edf/windows-cloud-plugin/badge.svg?branch=master)](https://coveralls.io/github/groupe-edf/windows-cloud-plugin?branch=master) 
[![Join the chat at https://gitter.im/jenkinsci/windows-cloud-plugin](https://badges.gitter.im/jenkinsci/windows-cloud-plugin.svg)](https://gitter.im/jenkinsci/windows-cloud-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)


This plugin builds Windows agents to perform builds.

It has been tested on VMware virtual machines with Windows Server 2012 R2 and Windows Server 2016.

**Team :**

- [feirychris](https://github.com/feirychris)

- [Aelotmani](https://github.com/Aelotmani)

- [mat1e](https://github.com/mat1e)


## Requirements
JNLP must be enabled on Jenkins.

**Make sure you have WinRM configured on your server with NTLM or Basic authentication.**
See [Microsoft documentation](https://docs.microsoft.com/en-us/windows/win32/winrm/installation-and-configuration-for-windows-remote-management)

Java must be installed on the server and included in Path environment variable.

### WinRM Authorization Configuration

In a PowerShell terminal,

Check Winrm configuration :

```
PS C:\Users\admsrv> winrm get winrm/config
Config
(...)
    Service
        RootSDDL = O:NSG:BAD:P(A;;GA;;;BA)(A;;GR;;;IU)(A;;GX;;;RM)S:P(AU;FA;GA;;;WD)(AU;SA;GXGW;;;WD)
        MaxConcurrentOperations = 4294967295
        MaxConcurrentOperationsPerUser = 1500
        EnumerationTimeoutms = 240000
        MaxConnections = 300
        MaxPacketRetrievalTimeSeconds = 120
        AllowUnencrypted = false
        Auth
            Basic = false // --> NTLM or Basic must be enabled
            Kerberos = true
            Negotiate = true // --> NTLM or Basic must be enabled
            Certificate = false
            CredSSP = true
            CbtHardeningLevel = Relaxed
        DefaultPorts
            HTTP = 5985 // --> Port for http
            HTTPS = 5986 // --> Port for https
        IPv4Filter = *
        IPv6Filter = *
        EnableCompatibilityHttpListener = false
        EnableCompatibilityHttpsListener = false
        CertificateThumbprint
        AllowRemoteAccess = true
(...)
```

Verify PowerShell permissions :

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

In the "Configure Clouds" section of Jenkins Nodes Configuration.

Add a new Windows cloud

<img src="https://zupimages.net/up/20/50/bs3n.png" width="300"/>

Give a unique name to the cloud. Then click on "Cloud Details..."

In the Agent Properties section, fill the Jenkins URL. Then add a new Windows Host and click on "Host Details..."

Fill the fields like in the given example :

<img src="https://zupimages.net/up/20/50/u1ce.png" width="700"/>

**The Credential is an username and password type and the account used must be in Administrator group on the server.**

Click on "Test Connection" to test the configuration. If it works, you should see the name of the windows computer and the user used to connect.

<img src="https://zupimages.net/up/20/50/buor.png" width="500"/>

After configure a job to run on this host, you should see Jenkins agents created

<img src="https://zupimages.net/up/20/50/5cvf.png" width="300"/>

## Troubleshooting

The plugin was tested on a VMware virtual machine with 2 processors and 8GB Memory.
The memory was wide, but the processor touched 100% many times.

When it happens, the creation of the agents can be slow or sometimes, it cannot be created before the timeout. 

In this case, it is better to reduce the number of users allowed on the Host in the cloud configuration. If you really want to keep the max users, you can upgrade "Agent connection timeout", "Command timeout" in advanced properties of the host. 


## References

This project contains code under Apache-2.0 License from :

 - [cloudsoft/winrm4j](https://github.com/cloudsoft/winrm4j) for WinRM authentication
 - [sshoogr/groovy-winrm-client](https://github.com/sshoogr/groovy-winrm-client) for WinRM requests
 
## Contact

Any question ? You can ask it on the [Gitter](https://gitter.im/jenkinsci/windows-cloud-plugin) room or open an issue.
