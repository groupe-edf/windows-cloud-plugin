import org.apache.http.client.config.AuthSchemes

import fr.edf.jenkins.plugins.windows.Messages



def f = namespace(lib.FormTagLib)

f.entry(title: Messages.Host_Host(), field:'host') {
    f.textbox(clazz: 'required', checkMethod: 'post')
}

f.entry(title: Messages.Host_Disable() , field:'disable') {
    f.checkbox()
}

f.advanced(title:Messages.Host_Details()) {
    
        f.entry(title:Messages.Host_Label(), field:'label') {
            f.textbox()
        }
    
        f.entry(title: Messages.Host_MaxTries(), field: 'maxTries') {
            f.number(clazz: 'required', min: 1, default: 5)
        }
    
        f.entry(title: Messages.Host_Port(), field: 'port') {
            f.number(clazz: 'required', default: 5895, min: 1)
        }
        
        f.entry(title: Messages.Host_AuthenticationScheme(), field: 'authenticationScheme'){
            f.select(default: AuthSchemes.NTLM)
        }
    
    
        f.entry(title: Messages.Host_MaxUsers(), field: 'maxUsers') {
            f.number(clazz: 'required', min: 1)
        }
    
    
        f.block() {
            f.validateButton(
                    title: Messages.Host_TestConnection(),
                    progress: 'Testing...',
                    method: 'verifyConnection',
                    with: 'host,port'
                    )
        }
    
    
    }
    

