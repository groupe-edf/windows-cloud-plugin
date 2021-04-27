import fr.edf.jenkins.plugins.windows.Messages
import org.apache.http.client.config.AuthSchemes

def f = namespace(lib.FormTagLib)
def c = namespace(lib.CredentialsTagLib)

f.entry(title: Messages.Host_useHttps(), field: 'useHttps') {
    f.checkbox(default: false)
}

f.entry(title: 'Disable certificate check', field: 'disableCertificateCheck') {
    f.checkbox(default: false)
}

f.entry(title: Messages.Host_Port(), field: 'port') {
    f.number(clazz: 'required', default: 5985, min: 1)
}

f.entry(title: Messages.Host_AuthenticationScheme(), field: 'authenticationScheme'){
    f.select(default: AuthSchemes.NTLM)
}

f.entry(title:Messages.Host_Credentials(), field:'credentialsId'){
    c.select(context: app, , includeUser: false, expressionAllowed: false)
}

f.block() {
    f.validateButton(
            title: 'Test Connection',
            progress: 'Testing...',
            method: 'verifyConnection',
            with: 'host,port,credentialsId,authenticationScheme,useHttps,disableCertificateCheck,connectionTimeout,readTimeout'
            )
}

f.entry(title: Messages.Host_MaxTries(), field: 'maxTries') {
    f.number(clazz: 'required', min: 1, default: 5)
}

f.entry(title: Messages.Host_ConnectionTimeout(), field: 'connectionTimeout'){
    f.number(clazz: 'required', default: 15, min: 5)
}

f.entry(title: Messages.Host_ReadTimeout(), field: 'readTimeout'){
    f.number(clazz: 'required', default: 60, min: 30)
}

f.advanced('Advanced settings') {
    f.entry(title: Messages.Host_AgentConnectionTimeout(), field: 'agentConnectionTimeout'){
        f.number(clazz: 'required', default: 60, min: 15)
    }

    f.entry(title: Messages.Host_CommandTimeout(), field: 'commandTimeout'){
        f.number(clazz: 'required', default: 20, min: 10)
    }
}

f.entry(title: Messages._Cloud_JenkinsUrl(), field: 'jenkinsUrl'){
    f.textbox()
}