package fr.edf.jenkins.plugins.windows.util

import org.antlr.v4.runtime.misc.NotNull
/**
 * Jenkins UI form
 * @author CHRIS BAHONDA
 *
 */
class FormUtils {

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
}
