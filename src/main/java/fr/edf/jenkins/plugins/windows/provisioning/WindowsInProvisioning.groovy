package fr.edf.jenkins.plugins.windows.provisioning

import javax.annotation.CheckForNull

import fr.edf.jenkins.plugins.windows.agent.WindowsAgent
import hudson.Extension
import hudson.model.Computer
import hudson.model.Label
import hudson.model.Node

@Extension
class WindowsInProvisioning extends InProvisioning{

    private static boolean isNotAcceptingTasks(Node n) {
        Computer computer = n.toComputer()
        return computer != null && (computer.isLaunchSupported()
                || !computer.isAcceptingTasks() // Launcher has not been called yet
                || !n.isAcceptingTasks()) // node is not ready yet
    }

    @Override
    Set<String> getInProvisioning(@CheckForNull Label label) {
        if (label != null) {
            return label.getNodes().stream()
                    .filter { node ->
                        node instanceof WindowsAgent
                    }
                    .filter { node ->
                        WindowsInProvisioning.isNotAcceptingTasks(node)
                    }
                    .collect([] as HashSet){it.name}
        } else {
            return Collections.emptySet();
        }
    }
}
