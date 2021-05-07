package fr.edf.jenkins.plugins.windows.provisioning

import javax.annotation.CheckForNull
import javax.annotation.Nonnull

import hudson.ExtensionList
import hudson.ExtensionPoint
import hudson.model.Label

/**
 * 
 * @author Mathieu Delrocq
 *
 */
abstract class InProvisioning implements ExtensionPoint{

    /**
     * Returns the agents names in provisioning according to all implementations of this extension point for the given label
     * @param label the {@link Label} being checked
     * @return the agents names in provisioning according to all implementations of this extension point for the given label
     */
    @Nonnull
    static Set<String> getAllInProvisioning(@CheckForNull Label label) {
        return all().collect{ it.getInProvisioning(label) }.collectMany([] as HashSet){ it }
    }


    static ExtensionList<InProvisioning> all() {
        return ExtensionList.lookup(InProvisioning)
    }

    /**
     * Returns the agents in provisioning for the current label
     * @param label the label being checked
     * @return The agents names in provisioning for the current label
     */
    @Nonnull
    abstract Set<String> getInProvisioning(Label label)
}
