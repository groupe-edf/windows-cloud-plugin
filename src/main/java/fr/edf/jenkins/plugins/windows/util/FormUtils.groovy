package fr.edf.jenkins.plugins.windows.util

import org.antlr.v4.runtime.misc.NotNull
import org.kohsuke.stapler.AncestorInPath
import org.kohsuke.stapler.QueryParameter

import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.common.StandardListBoxModel

import hudson.model.Item
import hudson.security.ACL
import hudson.util.ListBoxModel
import jenkins.model.Jenkins

class FormUtils {

    /**
     * Change the host into URI
     * @param String host
     * @return URI
     */

    static URI getUri(@NotNull String host) {
        if (!(host.startsWith("http://") || host.startsWith("https://"))) {
            host = "http://" + host
        }
        if (!host.endsWith("/")) {
            host += "/"
        }
        if(host.startsWith("http:")) {
            host = "http://" + host
        }
        try {
            return new URI(host)
        } catch(Exception e) {
            return null
        }
    }


//    ListBoxModel newWindowsHostCredentialsItemsListBoxModel(@QueryParameter String host, @QueryParameter String credentialsId,
//        @AncestorInPath Item item) {
//        Jenkins jenkins = Jenkins.get();
//        StandardListBoxModel result = new StandardListBoxModel()
//        boolean notAdmin = item == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER)
//        boolean noCredentials = item != null && !item.hasPermission(Item.EXTENDED_READ) &&
//                !item.hasPermission(CredentialsProvider.USE_ITEM)
//
//        if(notAdmin || noCredentials) {
//            return result.includeCurrentValue(credentialsId)
//        }
//        return result
//                .includeEmptyValue()
//                .includeMatchingAs(ACL.SYSTEM,
//                jenkins,
//                StandardCredentials.class,
//                fromUri(getUri(host).toString().build()),
//                CredentialsMatchers.always())
//    }
}

