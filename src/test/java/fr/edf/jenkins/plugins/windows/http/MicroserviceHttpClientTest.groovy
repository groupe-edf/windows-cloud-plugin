package fr.edf.jenkins.plugins.windows.http

import org.apache.http.HttpException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient

import fr.edf.jenkins.plugins.windows.WindowsUser
import fr.edf.jenkins.plugins.windows.pojos.WindowsPojoBuilder
import hudson.util.Secret
import spock.lang.Specification

class MicroserviceHttpClientTest extends Specification {


    String url = "testUrl"
    Integer port = 1011
    String contextPath = "/testPath"
    Secret token = Secret.fromString("token")


    def "get httpClient work"() {
        given:
        MicroserviceHttpClient client = new MicroserviceHttpClient(
                url,
                port,
                contextPath,
                token,
                true,
                true,
                15,
                30
                )

        when:
        HttpClient httpClient = client.getHttpClient()

        then:
        notThrown Exception
        assert httpClient != null
        assert httpClient instanceof CloseableHttpClient
    }

    def "Should throws HttpException"() {
        given:
        CloseableHttpClient httpClient = Stub ()

        MicroserviceHttpClient client = new MicroserviceHttpClient(url, port, contextPath, token, false, false, 15, 30)

        when:
        ExecutionResult result = client.whoami()
        System.out.println("Execution Result : \n Code : $result.code \n Output : $result.output \n Error : $result.error")

        then:
        HttpException he = thrown()
        assert he.getMessage().contains("An error occured when performing the request http://testUrl:1011/testPath/api/whoami")
        assert he.getCause() instanceof UnknownHostException
    }

    def "whoami should works"() {
        given:
        ExecutionResult givenResult = new ExecutionResult(code:200, error:"none",output:"whoami ok")
        CloseableHttpClient httpClient = Stub ()
        httpClient.execute(*_) >> givenResult

        MicroserviceHttpClient client = Spy(MicroserviceHttpClient,constructorArgs: [url, port, contextPath, token, false, false, 15, 30]) {
            getHttpClient() >> httpClient
        }


        when:
        ExecutionResult result = client.whoami()
        System.out.println("Execution Result : \n Code : $result.code \n Output : $result.output \n Error : $result.error")

        then:
        notThrown Exception
        assert result.code == givenResult.code
        assert result.error.equals(givenResult.error)
        assert result.output.equals(givenResult.output)
    }

    def "listUsers should works"() {
        given:
        ExecutionResult givenResult = new ExecutionResult(code:200, error:"none",output:"list user ok")
        CloseableHttpClient httpClient = Stub ()
        httpClient.execute(*_) >> givenResult

        MicroserviceHttpClient client = Spy(MicroserviceHttpClient,constructorArgs: [url, port, contextPath, token, false, false, 15, 30]) {
            getHttpClient() >> httpClient
        }

        when:
        ExecutionResult result = client.listUser()
        System.out.println("Execution Result : \n Code : $result.code \n Output : $result.output \n Error : $result.error")

        then:
        notThrown Exception
        assert result.code == givenResult.code
        assert result.error.equals(givenResult.error)
        assert result.output.equals(givenResult.output)
    }

    def "createUser should works"() {
        given:
        WindowsUser user = WindowsPojoBuilder.buildUser()
        ExecutionResult givenResult = new ExecutionResult(code:200, error:"none",output:"create user ok")
        CloseableHttpClient httpClient = Stub ()
        httpClient.execute(*_) >> givenResult

        MicroserviceHttpClient client = Spy(MicroserviceHttpClient,constructorArgs: [url, port, contextPath, token, false, false, 15, 30]) {
            getHttpClient() >> httpClient
        }

        when:
        ExecutionResult result = client.createUser(user)
        System.out.println("Execution Result : \n Code : $result.code \n Output : $result.output \n Error : $result.error")

        then:
        notThrown Exception
        assert result.code == givenResult.code
        assert result.error.equals(givenResult.error)
        assert result.output.equals(givenResult.output)
    }

    def "deleteUser should works"() {
        given:
        WindowsUser user = WindowsPojoBuilder.buildUser()
        ExecutionResult givenResult = new ExecutionResult(code:200, error:"none",output:"delete user ok")
        CloseableHttpClient httpClient = Stub ()
        httpClient.execute(*_) >> givenResult

        MicroserviceHttpClient client = Spy(MicroserviceHttpClient,constructorArgs: [url, port, contextPath, token, false, false, 15, 30]) {
            getHttpClient() >> httpClient
        }

        when:
        ExecutionResult result = client.deleteUser(user.username)
        System.out.println("Execution Result : \n Code : $result.code \n Output : $result.output \n Error : $result.error")

        then:
        notThrown Exception
        assert result.code == givenResult.code
        assert result.error.equals(givenResult.error)
        assert result.output.equals(givenResult.output)
    }

    def "getRemoting should works"() {
        given:
        WindowsUser user = WindowsPojoBuilder.buildUser()
        ExecutionResult givenResult = new ExecutionResult(code:200, error:"none",output:"get remoting ok")
        CloseableHttpClient httpClient = Stub ()
        httpClient.execute(*_) >> givenResult

        MicroserviceHttpClient client = Spy(MicroserviceHttpClient,constructorArgs: [url, port, contextPath, token, false, false, 15, 30]) {
            getHttpClient() >> httpClient
        }

        when:
        ExecutionResult result = client.getRemoting(user, "jenkinsUrl")
        System.out.println("Execution Result : \n Code : $result.code \n Output : $result.output \n Error : $result.error")

        then:
        notThrown Exception
        assert result.code == givenResult.code
        assert result.error.equals(givenResult.error)
        assert result.output.equals(givenResult.output)
    }

    def "jnlp should works"() {
        given:
        WindowsUser user = WindowsPojoBuilder.buildUser()
        ExecutionResult givenResult = new ExecutionResult(code:200, error:"none",output:"jnlp ok")
        CloseableHttpClient httpClient = Stub ()
        httpClient.execute(*_) >> givenResult

        MicroserviceHttpClient client = Spy(MicroserviceHttpClient,constructorArgs: [url, port, contextPath, token, false, false, 15, 30]) {
            getHttpClient() >> httpClient
        }

        when:
        ExecutionResult result = client.connectJnlp(user, "jenkinsUrl", "secret")
        System.out.println("Execution Result : \n Code : $result.code \n Output : $result.output \n Error : $result.error")

        then:
        notThrown Exception
        assert result.code == givenResult.code
        assert result.error.equals(givenResult.error)
        assert result.output.equals(givenResult.output)
    }
}
