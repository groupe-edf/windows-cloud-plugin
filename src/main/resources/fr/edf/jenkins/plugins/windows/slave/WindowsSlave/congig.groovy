import fr.edf.jenkins.plugins.windows.Messages

def f = namespace(lib.FormTagLib)

f.entry(title: Messages.WindowsSlave_Description(), help:'/help/system-config/master-slave/description.html'){
    f.textbox(field: 'nodeDescription')
}

f.entry(title: Messages._WindowsSlave_Executors(), field: 'numExecutors'){
    f.textbox()
}

f.entry(title: Messages.WindowsSlave_RemoteFSRoot(), field: 'remoteFS'){
    f.textbox()
}

f.slave_mode(name: 'mode', node: it)

f.descriptorList(title: Messages._WindowsSlave_NodeProperties(), descriptors: h.getNodePropertyDescriptors(descriptor.clazz), field: 'nodeProperties')