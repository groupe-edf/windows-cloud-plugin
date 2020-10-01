import org.apache.http.client.config.AuthSchemes

def f = namespace(lib.FormTagLib)

f.entry(title: 'Host', field:'host') {
    f.textbox(clazz: 'required', checkMethod: 'post')
}

f.entry(title: 'Disable', field:'disable') {
    f.checkbox()
}

f.advanced(title:'Windows Host Details') {
    
        f.entry(title:'Label', field:'label') {
            f.textbox()
        }
    
        f.entry(title: 'Maximum Tries', field: 'maxTries') {
            f.number(clazz: 'required', min: 1, default: 5)
        }
    
        f.entry(title: 'Port Number', field: 'port') {
            f.number(clazz: 'required', default: 5895, min: 1)
        }
        
        f.entry(title: 'Authentication Scheme', field: 'authenticationScheme'){
            f.select(default: AuthSchemes.NTLM)
        }
    
    
        f.entry(title: 'Maximum Users', field: 'maxUsers') {
            f.number(clazz: 'required', min: 1)
        }
    
    
        f.block() {
            f.validateButton(
                    title: 'Test Connection',
                    progress: 'Testing...',
                    method: 'verifyConnection',
                    with: 'host,port'
                    )
        }
    
    
    }
    

