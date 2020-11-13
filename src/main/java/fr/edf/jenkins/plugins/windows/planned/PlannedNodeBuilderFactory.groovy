package fr.edf.jenkins.plugins.windows.planned

import hudson.ExtensionList
import hudson.ExtensionPoint

/**
 * A factory of {@link PlannedNodeBuilder} instances
 * @author CHRIS BAHONDA
 *
 */
abstract class PlannedNodeBuilderFactory implements ExtensionPoint{

    /**
     * Returns all registered implementations of {@link PlannedNodeBuilderFactory}
     * @return all registered implementations of {@link PlannedNodeBuilderFactory}
     */
    static ExtensionList<PlannedNodeBuilderFactory> all() {
        return ExtensionList.lookup(PlannedNodeBuilderFactory.class)
    }

    /**
     * Returns a new instance of {@link PlannedNodeBuilder}
     * @return a new instance of {@link PlannedNodeBuilder}
     */
    static PlannedNodeBuilder createInstance() {
        for (PlannedNodeBuilderFactory factory: all()) {
            PlannedNodeBuilder plannedNodeBuilder = factory.newInstance()
            if (plannedNodeBuilder != null) {
                return plannedNodeBuilder
            }
        }
        return new StandardPlannedNodeBuilder()
    }

    /**
     * Returns a new instance of {@link PlannedNodeBuilder}
     * @return a new instance of {@link PlannedNodeBuilder}
     */
    abstract PlannedNodeBuilder newInstance()
}
