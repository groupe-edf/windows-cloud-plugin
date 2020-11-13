import fr.edf.jenkins.plugins.windows.Messages

def f = namespace(lib.FormTagLib)

f.entry(title: Messages._Cloud_JenkinsUrl(), field: 'JenkinsUrl'){
    f.textbox()
}