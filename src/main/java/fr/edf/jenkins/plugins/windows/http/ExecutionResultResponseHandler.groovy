package fr.edf.jenkins.plugins.windows.http

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.annotation.Contract
import org.apache.http.annotation.ThreadingBehavior
import org.apache.http.client.HttpResponseException
import org.apache.http.impl.client.AbstractResponseHandler

import groovy.json.JsonSlurper

/**
 * A {@link org.apache.http.client.ResponseHandler} that returns the response body as a {@link ExecutionResult}
 * for successful (2xx) responses. If the response code was &gt;= 300, the response
 * body is consumed and an {@link org.apache.http.client.HttpResponseException} is thrown.
 * <p>
 * If this is used with
 * {@link org.apache.http.client.HttpClient#execute(
 *  org.apache.http.client.methods.HttpUriRequest, org.apache.http.client.ResponseHandler)},
 * HttpClient may handle redirects (3xx responses) internally.
 * </p>
 *
 * @since 4.0
 * 
 * @author Mathieu Delrocq
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
class ExecutionResultResponseHandler extends AbstractResponseHandler<ExecutionResult> {

    /** array of success status of an HTTP response */
    private static final List<Integer> SUCCESS_STATUS = [200, 201, 202, 204]

    /**
     * Returns the entity as a body as a {@link ExecutionResult}.
     */
    @Override
    public ExecutionResult handleEntity(final HttpEntity entity) throws IOException {
        InputStream stream
        ExecutionResult result
        try {
            stream = entity.getContent()
            result = new ExecutionResult(new JsonSlurper().parse(stream))
        } finally {
            stream.close()
        }
        return result
    }

    /**
     * Returns the response as {@link ExecutionResult}.
     */
    @Override
    public ExecutionResult handleResponse(final HttpResponse response) throws HttpResponseException, IOException {
        if(!SUCCESS_STATUS.contains(response.statusLine.statusCode)) {
            String message = "$response.statusLine.statusCode : $response.statusLine.reasonPhrase"
            throw new HttpResponseException(message)
        }
        return handleEntity(response.getEntity())
    }
}
