package fr.edf.jenkins.plugins.windows.util

import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder
import com.google.common.base.Preconditions

import hudson.model.ModelObject
import hudson.security.ACL


/**
 * Credentials
 * @author CHRIS BAHONDA
 *
 */
class CredentialsUtils {

    /**
     * Retrieve credentials via uri, host and id
     * @param host
     * @param credentialsId
     * @param context
     * @return credentials
     */
    static StandardCredentials findCredentials(final String host, final String credentialsId, ModelObject context) {
        Preconditions.checkNotNull(host, "host not set")
        URI uri = WindowsCloudUtils.getUri(host)
        Preconditions.checkNotNull(credentialsId, "CredentialsId not set")
        Preconditions.checkNotNull(uri, "uri not set")
        Preconditions.checkNotNull(context, "context not set")
        Preconditions.checkArgument(!credentialsId.isEmpty())

        def credentials = CredentialsMatchers.firstOrNull(CredentialsProvider.lookupCredentials(StandardCredentials.class,
                context,
                ACL.SYSTEM,
                URIRequirementBuilder.fromUri(uri.toString()).build()),
                CredentialsMatchers.withId(credentialsId))

        Preconditions.checkArgument(credentialsId != null)
        return credentials
    }
}
