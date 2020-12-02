package fr.edf.jenkins.plugins.windows.pojos

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.ProtocolVersion
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicHttpResponse
import org.apache.http.message.BasicStatusLine
import org.apache.http.protocol.HTTP

class WinRMResponseBuilder {

    private static buildHeader() {
        return new BasicHeader(HTTP.CONTENT_TYPE, "application/soap+xml; charset=UTF-8")
    }

    static HttpResponse buildHttpResponse() {
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1)
        BasicStatusLine basicStatusLine = new BasicStatusLine(protocolVersion, 200, "OK")
        return new BasicHttpResponse(basicStatusLine)
    }

    static HttpEntity buildOpenShellResponseEntity(String shellId) {
        StringBuilder builder = new StringBuilder('<s:Envelope xml:lang="en-US" ')
        builder.append('xmlns:s="http://www.w3.org/2003/05/soap-envelope" ')
        builder.append('xmlns:a="http://schemas.xmlsoap.org/ws/2004/08/addressing" ')
        builder.append('xmlns:x="http://schemas.xmlsoap.org/ws/2004/09/transfer" ')
        builder.append('xmlns:w="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd" ')
        builder.append('xmlns:rsp="http://schemas.microsoft.com/wbem/wsman/1/windows/shell" ')
        builder.append('xmlns:p="http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd">')
        builder.append('<s:Header>')
        builder.append('<a:Action>http://schemas.xmlsoap.org/ws/2004/09/transfer/CreateResponse</a:Action>')
        builder.append('<a:MessageID>uuid:068F2ED6-0C90-42AC-988B-596AD73803D1</a:MessageID>')
        builder.append('<a:To>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</a:To>')
        builder.append('<a:RelatesTo>uuid:68EC86A0-B214-4E06-B86D-673599B9864A</a:RelatesTo>')
        builder.append('</s:Header>')
        builder.append('<s:Body>')
        builder.append('<x:ResourceCreated>')
        builder.append('<a:Address>https://127.0.0.1:5986/wsman</a:Address>')
        builder.append('<a:ReferenceParameters>')
        builder.append('<w:ResourceURI>http://schemas.microsoft.com/wbem/wsman/1/windows/shell/cmd</w:ResourceURI>')
        builder.append('<w:SelectorSet>')
        builder.append('<w:Selector Name="ShellId">')
        builder.append(shellId)
        builder.append('</w:Selector>')
        builder.append('</w:SelectorSet>')
        builder.append('</a:ReferenceParameters>')
        builder.append('</x:ResourceCreated>')
        builder.append('<rsp:Shell ')
        builder.append('xmlns:rsp="http://schemas.microsoft.com/wbem/wsman/1/windows/shell">')
        builder.append('<rsp:ShellId>')
        builder.append(shellId)
        builder.append('</rsp:ShellId>')
        builder.append('<rsp:ResourceUri>http://schemas.microsoft.com/wbem/wsman/1/windows/shell/cmd</rsp:ResourceUri>')
        builder.append('<rsp:Owner>windows\\user</rsp:Owner>')
        builder.append('<rsp:ClientIP>127.0.0.1</rsp:ClientIP>')
        builder.append('<rsp:IdleTimeOut>PT7200.000S</rsp:IdleTimeOut>')
        builder.append('<rsp:InputStreams>stdin</rsp:InputStreams>')
        builder.append('<rsp:OutputStreams>stdout stderr</rsp:OutputStreams>')
        builder.append('<rsp:ShellRunTime>P0DT0H0M0S</rsp:ShellRunTime>')
        builder.append('<rsp:ShellInactivity>P0DT0H0M0S</rsp:ShellInactivity>')
        builder.append('</rsp:Shell>')
        builder.append('</s:Body>')
        builder.append('</s:Envelope>')
        System.out.println(builder.toString())
        StringEntity entity = new StringEntity(builder.toString())
        entity.setContentType(buildHeader())
        return entity
    }

    static HttpEntity buildExecuteCommandResponseEntity(String commandId) {
        StringBuilder builder = new StringBuilder('<s:Envelope xml:lang="en-US" ')
        builder.append('xmlns:s="http://www.w3.org/2003/05/soap-envelope" ')
        builder.append('xmlns:a="http://schemas.xmlsoap.org/ws/2004/08/addressing" ')
        builder.append('xmlns:x="http://schemas.xmlsoap.org/ws/2004/09/transfer" ')
        builder.append('xmlns:w="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd" ')
        builder.append('xmlns:rsp="http://schemas.microsoft.com/wbem/wsman/1/windows/shell" ')
        builder.append('xmlns:p="http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd">')
        builder.append('<s:Header>')
        builder.append('<a:Action>http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandResponse</a:Action>')
        builder.append('<a:MessageID>uuid:F5ABE26A-832E-425E-9C7B-5205F6A83498</a:MessageID>')
        builder.append('<a:To>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</a:To>')
        builder.append('<a:RelatesTo>uuid:745A1CA3-805C-407F-BF1D-D531577F05F7</a:RelatesTo>')
        builder.append('</s:Header>')
        builder.append('<s:Body>')
        builder.append('<rsp:CommandResponse>')
        builder.append('<rsp:CommandId>')
        builder.append(commandId)
        builder.append('</rsp:CommandId>')
        builder.append('</rsp:CommandResponse>')
        builder.append('</s:Body>')
        builder.append('</s:Envelope>')
        System.out.println(builder.toString())
        StringEntity entity = new StringEntity(builder.toString())
        entity.setContentType(buildHeader())
        return entity
    }

    static HttpEntity buildGetCommandOutputResponseEntity(String commandId, String output) {
        StringBuilder builder = new StringBuilder('<s:Envelope xml:lang="en-US" ')
        builder.append('xmlns:s="http://www.w3.org/2003/05/soap-envelope" ')
        builder.append('xmlns:a="http://schemas.xmlsoap.org/ws/2004/08/addressing" ')
        builder.append('xmlns:w="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd" ')
        builder.append('xmlns:rsp="http://schemas.microsoft.com/wbem/wsman/1/windows/shell" ')
        builder.append('xmlns:p="http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd">')
        builder.append('<s:Header>')
        builder.append('<a:Action>http://schemas.microsoft.com/wbem/wsman/1/windows/shell/ReceiveResponse</a:Action>')
        builder.append('<a:MessageID>uuid:193248C1-41F1-4305-843A-EFEADA0F4F39</a:MessageID>')
        builder.append('<a:To>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</a:To>')
        builder.append('<a:RelatesTo>uuid:22BF96E7-DC5D-45B8-AC42-C2E1AE166B43</a:RelatesTo>')
        builder.append('</s:Header>')
        builder.append('<s:Body>')
        builder.append('<rsp:ReceiveResponse>')
        builder.append('<rsp:Stream Name="stdout" CommandId="'+commandId+'">')
        builder.append(output)
        builder.append('</rsp:Stream>')
        builder.append('<rsp:Stream Name="stdout" CommandId="'+commandId+'" End="true"></rsp:Stream>')
        builder.append('<rsp:Stream Name="stderr" CommandId="'+commandId+'" End="true"></rsp:Stream>')
        builder.append('<rsp:CommandState CommandId="'+commandId+'" State="http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done">')
        builder.append('<rsp:ExitCode>0</rsp:ExitCode>')
        builder.append('</rsp:CommandState>')
        builder.append('</rsp:ReceiveResponse>')
        builder.append('</s:Body>')
        builder.append('</s:Envelope>')

        System.out.println(builder.toString())
        StringEntity entity = new StringEntity(builder.toString())
        entity.setContentType(buildHeader())
        return entity
    }
    static HttpEntity buildCloseShellResponseEntity() {
        StringBuilder builder = new StringBuilder('<s:Envelope xml:lang="en-US" ')
        builder.append('xmlns:s="http://www.w3.org/2003/05/soap-envelope" ')
        builder.append('xmlns:a="http://schemas.xmlsoap.org/ws/2004/08/addressing" ')
        builder.append('xmlns:w="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd" ')
        builder.append('xmlns:p="http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd">')
        builder.append('<s:Header>')
        builder.append('<a:Action>http://schemas.xmlsoap.org/ws/2004/09/transfer/DeleteResponse</a:Action>')
        builder.append('<a:MessageID>uuid:A194577E-E1F9-4001-A147-B21009ADA7B5</a:MessageID>')
        builder.append('<a:To>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</a:To>')
        builder.append('<a:RelatesTo>uuid:1EB9BB41-E8B4-4C98-8225-8CEC9568ACBB</a:RelatesTo>')
        builder.append('</s:Header>')
        builder.append('<s:Body></s:Body>')
        builder.append('</s:Envelope>')

        System.out.println(builder.toString())
        StringEntity entity = new StringEntity(builder.toString())
        entity.setContentType(buildHeader())
        return entity
    }

    static HttpEntity buildCleanupCommandResponse() {
        StringBuilder builder = new StringBuilder('<s:Envelope ')
        builder.append('xml:lang="en-US" ')
        builder.append('xmlns:s="http://www.w3.org/2003/05/soap-envelope" ')
        builder.append('xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing" ')
        builder.append('xmlns:wsman="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd">')
        builder.append('<s:Header>')
        builder.append('<wsa:Action>')
        builder.append('http://schemas.microsoft.com/wbem/wsman/1/windows/shell/SignalResponse')
        builder.append('</wsa:Action>')
        builder.append('<wsa:MessageID>')
        builder.append('uuid:FE802FF2-82FD-4406-AEBF-8A9466F0DBFE')
        builder.append('</wsa:MessageID>')
        builder.append('<wsa:To>')
        builder.append('http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous')
        builder.append('</wsa:To>')
        builder.append('<wsa:RelatesTo>uuid:23F5AAD4-9501-4070-A4F8-B216782DE466</wsa:RelatesTo>')
        builder.append('</s:Header>')
        builder.append('<s:Body>')
        builder.append('<rsp:SignalResponse')
        builder.append('xmlns:rsp="http://schemas.microsoft.com/wbem/wsman/1/windows/shell">')
        builder.append('</rsp:SignalResponse>')
        builder.append('</s:Body>')
        builder.append('</s:Envelope>')

        System.out.println(builder.toString())
        StringEntity entity = new StringEntity(builder.toString())
        entity.setContentType(buildHeader())
        return entity
    }
}
