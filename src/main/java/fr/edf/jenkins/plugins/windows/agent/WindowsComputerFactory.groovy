package fr.edf.jenkins.plugins.windows.agent

import hudson.ExtensionList
import hudson.ExtensionPoint

/**
 * A factory of {@link WindowsComputer} instances.
 * @author CHRIS BAHONDA
 *
 */
abstract class WindowsComputerFactory implements ExtensionPoint{

    /**
     * return all registered implementations of {@link WindowsComputerFactory}
     * @return all registered implementations of {@link WindowsComputerFactory}
     */
    static ExtensionList <WindowsComputerFactory> all(){
        return ExtensionList.lookup(WindowsComputerFactory.class)
    }

    /**
     * Creates a new instance of {@link WindowsComputer}
     * @param agent
     * @return
     */
    abstract WindowsComputer newInstance(WindowsAgent agent)

    /**
     * Returns a new instance of {@link WindowsComputer}
     * @param agent
     * @return a new instance of {@link WindowsComputer}
     */
    static WindowsComputer createInstance(WindowsAgent agent) {
        for (WindowsComputerFactory factory : all()) {
            WindowsComputer computer = factory.newInstance(agent)
            if(computer != null) {
                return computer
            }
        }
        return new WindowsComputer(agent)
    }
}
