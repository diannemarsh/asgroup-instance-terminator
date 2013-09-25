package com.sample;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.sample.autoscaling.config.Config;
import com.sample.autoscaling.rules.RuleHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {Config.class, TestConfig.class})
public class RuleHandlerIT {

    @Autowired
    private RuleHandler ruleHandler;

    @Value("${test.auto.scaling.group}")
    private String testAutoScalingGroupName;

    @Test
    public void testRuleHandler() {
        AutoScalingGroup autoScalingGroup = mock(AutoScalingGroup.class);
        when(autoScalingGroup.getAutoScalingGroupName()).thenReturn(testAutoScalingGroupName);
        assertEquals(autoScalingGroup.getAutoScalingGroupName(), testAutoScalingGroupName);
    }


}
