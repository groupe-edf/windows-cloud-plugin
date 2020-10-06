package fr.edf.jenkins.plugins.windows.util

import org.antlr.v4.runtime.misc.NotNull

class FormUtils {

    /**
     * Change the host into URI
     * @param String host
     * @return URI
     */

    static URI getUri(@NotNull String host) {
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

