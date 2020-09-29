def f = namespace(lib.FormTagLib)

f.entry(title: 'Name', field:'name') {
    f.textbox(default:'windows')
}

f.advanced(title:'Cloud Details') {
    f.entry(title:'Windows Host') {
        f.repeatableHeteroProperty(
        field:'windowsHosts',
        hasHeader: 'true',
        addCaption: 'add Windows host',
        deleteCaption: 'remove Windows host',
        oneEach:'false',
        repeatableDeleteButton:'true'
        )
    }
}