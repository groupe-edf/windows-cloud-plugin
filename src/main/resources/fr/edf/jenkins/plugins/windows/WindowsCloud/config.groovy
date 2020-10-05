import fr.edf.jenkins.plugins.windows.Messages
def f = namespace(lib.FormTagLib)

f.entry(title: Messages.Cloud_Name(), field:'name') {
    f.textbox(default:'Windows')
}

f.advanced(title: Messages.Cloud_Details()) {
    f.entry(title:Messages.Host_DefaultName()) {
        f.repeatableHeteroProperty(
        field:'windowsHosts',
        hasHeader: 'true',
        addCaption: 'Add Windows host',
        deleteCaption: 'Remove Windows host',
        oneEach:'false',
        repeatableDeleteButton:'true'
        )
    }
}