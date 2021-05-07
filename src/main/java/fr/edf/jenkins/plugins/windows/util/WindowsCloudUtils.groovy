package fr.edf.jenkins.plugins.windows.util

import org.antlr.v4.runtime.misc.NotNull

import jenkins.model.Jenkins
/**
 * Utils for windows-cloud plugin
 * @author CHRIS BAHONDA
 * @author Mathieu Delrocq
 *
 */
class WindowsCloudUtils {

    /**
     * Change the host into URI
     * @param String host
     * @return URI
     */

    static URI getUri(@NotNull String host) {
        if(!host) return null
        if (!(host.startsWith("http://"))) {
            host = "http://" + host
        }
        if (!host.endsWith("/")) {
            host += "/"
        }
        try {
            return new URI(host)
        } catch(Exception e) {
            return null
        }
    }

    /**
     * Check and return a correct Jenkins URL
     * 
     * @param jenkinsUrl
     * @return Jenkins url
     */
    static String checkJenkinsUrl(@NotNull String jenkinsUrl) {
        jenkinsUrl = jenkinsUrl ?: Jenkins.get().getRootUrl()
        if(!jenkinsUrl.endsWith("/")) {
            jenkinsUrl += "/"
        }
        return jenkinsUrl
    }
}
