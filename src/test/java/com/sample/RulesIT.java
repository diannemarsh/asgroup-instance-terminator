package com.sample;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.Instance;
import com.sample.autoscaling.config.Config;
import com.sample.autoscaling.rules.AutoScalingGroupInstanceSelectionRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {Config.class, TestConfig.class})
public class RulesIT {

    @Value("${test.auto.scaling.group}")
    private String testAutoScalingGroupName;

    @Autowired
    private AutoScalingGroupInstanceSelectionRule autoScalingGroupInstancesCountRule;

    @Autowired
    private AutoScalingGroupInstanceSelectionRule autoScalingGroupActivityRule;

    @Test
    public void testCountRuleSuccess() throws InterruptedException {
        //Mock AutoScaling Group
        AutoScalingGroup autoScalingGroup = mock(AutoScalingGroup.class);
        when(autoScalingGroup.getAutoScalingGroupName()).thenReturn(testAutoScalingGroupName);
        //Mock instances in auto scale group
        Instance instance = mock(Instance.class);
        List<Instance> instanceList = mock(List.class);
        instanceList.add(instance);
        when(autoScalingGroup.getInstances()).thenReturn(instanceList);
        when(autoScalingGroup.getInstances().size()).thenReturn(2);
        assertTrue(autoScalingGroupInstancesCountRule.apply(autoScalingGroup));
    }

    @Test
    public void testActivityRuleSuccess() throws InterruptedException {
        //Mock AutoScaling Group
        AutoScalingGroup autoScalingGroup = mock(AutoScalingGroup.class);
        when(autoScalingGroup.getAutoScalingGroupName()).thenReturn(testAutoScalingGroupName);
        assertTrue(autoScalingGroupActivityRule.apply(autoScalingGroup));
    }

}
