package fr.edf.jenkins.plugins.windows.agent

import java.util.logging.Level
import java.util.logging.Logger

import javax.annotation.CheckForNull

import com.google.common.base.Objects

import fr.edf.jenkins.plugins.windows.WindowsCloud
import fr.edf.jenkins.plugins.windows.WindowsHost
import hudson.model.Executor
import hudson.model.Queue
import hudson.model.Queue.Task
import hudson.slaves.AbstractCloudComputer

/**
 * 
 * @author Mathieu Delrocq
 *
 */
class WindowsComputer extends AbstractCloudComputer<WindowsAgent>{

    private static final Logger LOGGER = Logger.getLogger(WindowsComputer.class.name)

    WindowsComputer(WindowsAgent node){
        super(node)
    }

    /**
     * {@inheritDoc}
     */
    @CheckForNull
    @Override
    WindowsAgent getNode() {
        return (WindowsAgent) super.getNode()
    }

    @CheckForNull
    WindowsCloud getCloud() {
        final WindowsAgent node = getNode()
        return node == null ?: node.getCloud()
    }

    @CheckForNull
    String getUserId() {
        final WindowsAgent node = getNode()
        return node == null ?: node.getUserId()
    }

    @CheckForNull
    String getCloudId() {
        final WindowsAgent node = getNode()
        return node == null ?: node.getCloudId()
    }

    @CheckForNull
    WindowsHost getWindowsHost() {
        final WindowsAgent node = getNode()
        return node == null ?: node.getHost()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void taskAccepted(Executor executor, Queue.Task task) {
        super.taskAccepted(executor, task)
        Queue.Executable exec = executor.getCurrentExecutable()
        LOGGER.log(Level.FINE, "Computer {0} accepted task {1}", this, exec)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void taskCompleted(Executor executor, Task task, long durationMS) {
        Queue.Executable exec = executor.getCurrentExecutable()
        LOGGER.log(Level.FINE, "Computer {0} completed task {1}", this, exec)
        super.taskCompleted(executor, task, durationMS)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void taskCompletedWithProblems(Executor executor, Task task, long durationMS, Throwable problems) {
        Queue.Executable exec = executor.getCurrentExecutable()
        LOGGER.log(Level.FINE, "Computer {0} completed task {1} with problems", this, exec)
        super.taskCompletedWithProblems(executor, task, durationMS, problems)
    }

    @Override
    String toString() {
        return Objects.toStringHelper(this)
                .add("name", super.getName())
                .add("agent", getNode())
                .toString()
    }
}
