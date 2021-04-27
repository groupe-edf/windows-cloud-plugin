import org.apache.http.client.config.AuthSchemes

import fr.edf.jenkins.plugins.windows.Messages

def f = namespace(lib.FormTagLib)

f.entry(title: Messages.Host_Disable() , field:'disable') {
    f.checkbox()
}

f.entry(title: Messages.Host_Host(), field:'host') {
    f.textbox(clazz: 'required', checkMethod: 'post')
}

f.advanced(title:Messages.Host_Details()) {

    f.entry(title:Messages.Host_Label(), field:'label') {
        f.textbox()
    }

    f.entry(title: Messages.Host_MaxUsers(), field: 'maxUsers') {
        f.number(clazz: 'required', min: 1, default: 5)
    }

    f.dropdownDescriptorSelector(title:'Connect method', field:'connector')

    f.entry(title: _(Messages.EnvVar_Title())) {
        f.repeatableHeteroProperty(
                field:'envVars',
                hasHeader: 'true',
                addCaption: Messages.EnvVar_Add(),
                deleteCaption:Messages.EnvVar_Delete(),
                oneEach:'false',
                repeatableDeleteButton:'true'
                )
    }
}
