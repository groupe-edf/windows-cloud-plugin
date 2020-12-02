package fr.edf.jenkins.plugins.windows.slave

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
     * @param slave
     * @return
     */
    abstract WindowsComputer newInstance(WindowsSlave slave)

    /**
     * Returns a new instance of {@link WindowsComputer}
     * @param slave
     * @return a new instance of {@link WindowsComputer}
     */
    static WindowsComputer createInstance(WindowsSlave slave) {
        for (WindowsComputerFactory factory : all()) {
            WindowsComputer computer = factory.newInstance(slave)
            if(computer != null) {
                return computer
            }
        }
        return new WindowsComputer(slave)
    }
}
