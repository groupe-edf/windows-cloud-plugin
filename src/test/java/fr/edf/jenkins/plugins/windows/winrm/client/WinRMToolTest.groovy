package fr.edf.jenkins.plugins.windows.winrm.client

import org.apache.http.HttpResponse
import org.apache.http.ProtocolVersion
import org.apache.http.StatusLine
import org.apache.http.client.config.AuthSchemes
import org.apache.http.client.methods.HttpPost
import org.apache.http.message.BasicHttpResponse
import org.apache.http.message.BasicStatusLine
import org.apache.http.protocol.HTTP

import fr.edf.jenkins.plugins.windows.pojos.WinRMResponseBuilder
import fr.edf.jenkins.plugins.windows.pojos.WindowsPojoBuilder
import fr.edf.jenkins.plugins.windows.util.Constants
import fr.edf.jenkins.plugins.windows.winrm.client.output.CommandOutput
import fr.edf.jenkins.plugins.windows.winrm.client.request.OpenShellRequest
import fr.edf.jenkins.plugins.windows.winrm.client.request.WinRMRequest
import spock.lang.Specification

class WinRMToolTest extends Specification {

    def "buildHttpPostRequest should work"() {
        given:
        WinRMTool tool = WindowsPojoBuilder.buildWinRMTool()
        WinRMRequest request = new OpenShellRequest(tool.getUrl())

        when:
        HttpPost postRequest = tool.buildHttpPostRequest(request)

        then:
        notThrown Exception
        assert postRequest.entity.contentType.name == HTTP.CONTENT_TYPE
        assert postRequest.entity.contentType.value == WinRMTool.SOAP_REQUEST_CONTENT_TYPE
    }

    def "open shell request should return a shell id"() {
        given:
        String shellId = "C443F44F-28E4-486F-A5A1-12745F90CF5A"
        HttpResponse httpResponse = WinRMResponseBuilder.buildHttpResponse()
        httpResponse.setEntity(WinRMResponseBuilder.buildOpenShellResponseEntity(shellId))
        WinRMTool tool = Spy(WinRMTool, constructorArgs: ["127.0.0.1", 5986, "username", "password", AuthSchemes.NTLM, true, true, 1]) {
            performRequest(*_) >> httpResponse
        }

        when:
        String response = tool.openShell()

        then:
        notThrown Exception
        assert response == shellId
    }

    def "open shell shloud throw WinRMException with 401 Unauthorized"() {
        given:
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1)
        BasicStatusLine basicStatusLine = new BasicStatusLine(protocolVersion, 401, "Unauthorized")
        HttpResponse  httpResponse = new BasicHttpResponse(basicStatusLine)

        WinRMTool tool = Spy(WinRMTool, constructorArgs: ["127.0.0.1", 5986, "username", "password", AuthSchemes.NTLM, true, true, 1]) {
            performRequest(*_) >> httpResponse
        }

        String message = String.format(
                WinRMException.FORMATTED_MESSAGE,
                "OpenShell",
                basicStatusLine.getProtocolVersion(),
                basicStatusLine.getStatusCode(),
                basicStatusLine.getReasonPhrase())

        when:
        tool.openShell()

        then:
        WinRMException e = thrown()
        e.getMessage().contains(message)
    }

    def "executeCommand should return a command id"() {
        given:
        String shellId = "C443F44F-28E4-486F-A5A1-12745F90CF5A"
        String commandId = "34B95100-EC13-485F-B918-0BDFAFE26439"
        HttpResponse httpResponse = WinRMResponseBuilder.buildHttpResponse()
        httpResponse.setEntity(WinRMResponseBuilder.buildExecuteCommandResponseEntity(commandId))
        WinRMTool tool = Spy(WinRMTool, constructorArgs: ["127.0.0.1", 5986, "username", "password", AuthSchemes.NTLM, true, true, 1]) {
            performRequest(*_) >> httpResponse
        }

        when:
        String response = tool.executeCommand(shellId, Constants.WHOAMI)

        then:
        notThrown Exception
        assert response == commandId
    }

    def "executeCommand shloud throw WinRMException with 500 error"() {
        given:
        String shellId = "C443F44F-28E4-486F-A5A1-12745F90CF5A"
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1)
        BasicStatusLine basicStatusLine = new BasicStatusLine(protocolVersion, 500, "Error")
        HttpResponse  httpResponse = new BasicHttpResponse(basicStatusLine)

        WinRMTool tool = Spy(WinRMTool, constructorArgs: ["127.0.0.1", 5986, "username", "password", AuthSchemes.NTLM, true, true, 1]) {
            performRequest(*_) >> httpResponse
        }

        String message = String.format(
                WinRMException.FORMATTED_MESSAGE,
                "ExecuteCommand",
                basicStatusLine.getProtocolVersion(),
                basicStatusLine.getStatusCode(),
                basicStatusLine.getReasonPhrase())

        when:
        tool.executeCommand(shellId, Constants.WHOAMI)

        then:
        WinRMException e = thrown()
        e.getMessage().contains(message)
    }

    def "getCommandOutput should return a username"() {
        given:

        String shellId = "C443F44F-28E4-486F-A5A1-12745F90CF5A"
        String commandId = "34B95100-EC13-485F-B918-0BDFAFE26439"
        String out = "windows\\user"
        String encodedOut = out.getBytes().encodeBase64()
        HttpResponse httpResponse = WinRMResponseBuilder.buildHttpResponse()
        httpResponse.setEntity(WinRMResponseBuilder.buildGetCommandOutputResponseEntity(commandId, encodedOut))
        WinRMTool tool = Spy(WinRMTool, constructorArgs: ["127.0.0.1", 5986, "username", "password", AuthSchemes.NTLM, true, true, 1]) {
            performRequest(*_) >> httpResponse
        }

        when:
        CommandOutput output = tool.getCommandOutput(shellId, commandId)
        String result = output.output

        then:
        notThrown Exception
        assert result == out
    }

    def "getCommandOutput shloud throw WinRMException with 500 error"() {
        given:
        String shellId = "C443F44F-28E4-486F-A5A1-12745F90CF5A"
        String commandId = "34B95100-EC13-485F-B918-0BDFAFE26439"
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1)
        BasicStatusLine basicStatusLine = new BasicStatusLine(protocolVersion, 500, "Error")
        HttpResponse  httpResponse = new BasicHttpResponse(basicStatusLine)

        WinRMTool tool = Spy(WinRMTool, constructorArgs: ["127.0.0.1", 5986, "username", "password", AuthSchemes.NTLM, true, true, 1]) {
            performRequest(*_) >> httpResponse
        }

        String message = String.format(
                WinRMException.FORMATTED_MESSAGE,
                "GetCommandOutput",
                basicStatusLine.getProtocolVersion(),
                basicStatusLine.getStatusCode(),
                basicStatusLine.getReasonPhrase())

        when:
        tool.getCommandOutput(shellId, commandId)

        then:
        WinRMException e = thrown()
        e.getMessage().contains(message)
    }

    def "cleanup command should works"() {
        given:
        String shellId = "C443F44F-28E4-486F-A5A1-12745F90CF5A"
        String commandId = "34B95100-EC13-485F-B918-0BDFAFE26439"
        HttpResponse httpResponse = WinRMResponseBuilder.buildHttpResponse()
        httpResponse.setEntity(WinRMResponseBuilder.buildCloseShellResponseEntity())
        WinRMTool tool = Spy(WinRMTool, constructorArgs: ["127.0.0.1", 5986, "username", "password", AuthSchemes.NTLM, true, true, 1]) {
            performRequest(*_) >> httpResponse
        }

        when:
        tool.cleanupCommand(shellId, commandId)

        then:
        notThrown Exception
    }

    def "cleanup command shloud throw WinRMException with 500 error"() {
        given:
        String shellId = "C443F44F-28E4-486F-A5A1-12745F90CF5A"
        String commandId = "34B95100-EC13-485F-B918-0BDFAFE26439"
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1)
        BasicStatusLine basicStatusLine = new BasicStatusLine(protocolVersion, 500, "Error")
        HttpResponse  httpResponse = new BasicHttpResponse(basicStatusLine)

        WinRMTool tool = Spy(WinRMTool, constructorArgs: ["127.0.0.1", 5986, "username", "password", AuthSchemes.NTLM, true, true, 1]) {
            performRequest(*_) >> httpResponse
        }

        String message = String.format(
                WinRMException.FORMATTED_MESSAGE,
                "CleanupCommand",
                basicStatusLine.getProtocolVersion(),
                basicStatusLine.getStatusCode(),
                basicStatusLine.getReasonPhrase())

        when:
        tool.cleanupCommand(shellId, commandId)

        then:
        WinRMException e = thrown()
        e.getMessage().contains(message)
    }

    def "deleteShell should works"() {
        given:

        String shellId = "C443F44F-28E4-486F-A5A1-12745F90CF5A"
        HttpResponse httpResponse = WinRMResponseBuilder.buildHttpResponse()
        httpResponse.setEntity(WinRMResponseBuilder.buildCloseShellResponseEntity())
        WinRMTool tool = Spy(WinRMTool, constructorArgs: ["127.0.0.1", 5986, "username", "password", AuthSchemes.NTLM, true, true, 1]) {
            performRequest(*_) >> httpResponse
        }

        when:
        tool.deleteShellRequest(shellId)

        then:
        notThrown Exception
    }

    def "deleteShell shloud throw WinRMException with 500 error"() {
        given:
        String shellId = "C443F44F-28E4-486F-A5A1-12745F90CF5A"
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1)
        BasicStatusLine basicStatusLine = new BasicStatusLine(protocolVersion, 500, "Error")
        HttpResponse  httpResponse = new BasicHttpResponse(basicStatusLine)

        WinRMTool tool = Spy(WinRMTool, constructorArgs: ["127.0.0.1", 5986, "username", "password", AuthSchemes.NTLM, true, true, 1]) {
            performRequest(*_) >> httpResponse
        }

        String message = String.format(
                WinRMException.FORMATTED_MESSAGE,
                "DeleteShellRequest",
                basicStatusLine.getProtocolVersion(),
                basicStatusLine.getStatusCode(),
                basicStatusLine.getReasonPhrase())

        when:
        tool.deleteShellRequest(shellId)

        then:
        WinRMException e = thrown()
        e.getMessage().contains(message)
    }
}
