package fr.edf.jenkins.plugins.windows.strategy

import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import fr.edf.jenkins.plugins.windows.WindowsCloud
import fr.edf.jenkins.plugins.windows.pojos.WindowsPojoBuilder
import hudson.model.Computer
import hudson.model.Label
import hudson.model.LoadStatistics.LoadStatisticsSnapshot
import hudson.slaves.NodeProvisioner.PlannedNode
import hudson.slaves.NodeProvisioner.StrategyDecision
import hudson.slaves.NodeProvisioner.StrategyState
import spock.lang.Specification

class FastNodeProvisionerStrategyTest extends Specification{
    
    @Rule
    JenkinsRule rule = new JenkinsRule()
    
    private LoadStatisticsSnapshot buildSnapshot() {
        LoadStatisticsSnapshot.Builder builder = new LoadStatisticsSnapshot.Builder()
        builder.withQueueLength(1)
        builder.with((Computer) null)
        return builder.build()
    }
    
    def"should not throw exception even if it cannot connect to the host"(){
        given:
        final List<PlannedNode> r = new ArrayList<>()
        LoadStatisticsSnapshot snapshot = buildSnapshot()
        StrategyState state = GroovyStub(StrategyState) {
            getLabel() >> Label.parse("label").getAt(0)
            getSnapshot() >> snapshot
        }
        
        WindowsCloud cloud = new WindowsCloud("test", WindowsPojoBuilder.buildWindowsHost(), WindowsPojoBuilder.buildConnector(rule), 
            new Integer(1))
        rule.jenkins.clouds.add(cloud)
        
        when:
        FastNodeProvisionerStrategy provisioner = new FastNodeProvisionerStrategy()
        StrategyDecision decision = provisioner.apply(state)
        
        then:
        notThrown Exception
        decision == StrategyDecision.CONSULT_REMAINING_STRATEGIES
    }
    
    def"should throw exception"(){
        
    }
    
}
