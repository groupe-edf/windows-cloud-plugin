import fr.edf.jenkins.plugins.windows.Messages

def f = namespace(lib.FormTagLib)

f.entry(title: Messages.EnvVar_Key()(), field: 'key'){
    f.textbox()
}

f.entry(title: Messages.EnvVar_Value(), field: 'value'){
    f.textbox()
}