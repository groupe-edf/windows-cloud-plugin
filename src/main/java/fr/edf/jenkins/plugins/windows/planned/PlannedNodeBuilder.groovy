package fr.edf.jenkins.plugins.windows.planned

import fr.edf.jenkins.plugins.windows.WindowsCloud
import fr.edf.jenkins.plugins.windows.WindowsHost
import fr.edf.jenkins.plugins.windows.WindowsUser
import hudson.slaves.EnvironmentVariablesNodeProperty
import hudson.slaves.NodeProperty
import hudson.slaves.NodeProvisioner

/**
 * A builder of {@link hudson.slaves.NodeProvisioner.PlannedNode} implementations for Windows
 * Can be subclassed to provide alternative implementations of {@link hudson.slaves.NodeProvisioner.PlannedNode}
 * @author CHRIS BAHONDA
 *
 */
abstract class PlannedNodeBuilder {

    protected WindowsCloud cloud
    protected WindowsUser user
    protected WindowsHost windowsHost
    protected int numExecutors = 1
    protected List<? extends NodeProperty<?>> nodeProperties

    /**
     * @param cloud the {@link WindowsCloud} instance to use
     * @return the current builder
     */
    PlannedNodeBuilder cloud(WindowsCloud cloud) {
        this.cloud = cloud
        return this
    }

    /**
     * 
     * @param host the {@link WindowsHost} instance to use
     * @return the current builder
     */
    PlannedNodeBuilder host(WindowsHost host) {
        this.windowsHost = host
        if(host.envVars) {
            this.nodeProperties = new ArrayList()
            nodeProperties.add(new EnvironmentVariablesNodeProperty(host.envVars.collect {
                new EnvironmentVariablesNodeProperty.Entry(it.key, it.value)
            }))
        }
        else {
            this.nodeProperties = Collections.EMPTY_LIST
        }
        return this
    }

    /**
     * 
     * @param numExecutors : the number of executors
     * @return the current builder
     */
    PlannedNodeBuilder numExecutors(int numExecutors) {
        this.numExecutors = numExecutors
        return this
    }

    /**
     * Builds the {@link hudson.slaves.NodeProvisioner.PlannedNode} instance based on 
     * the given inputs
     * @return a {@link hudson.slaves.NodeProvisioner.PlannedNode} configured from
     * this builder
     */
    abstract NodeProvisioner.PlannedNode build()
}
