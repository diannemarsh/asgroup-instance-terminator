package com.sample;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nullable;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.Instance;
import com.google.common.util.concurrent.FutureCallback;
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

    /**
     * This is not a thread safe variable but this test will be run by single thread and the tests below (success and
     * fail) will be called one after the other
     */
    private boolean rulesResult = false;


    @Test
    public void testRuleHandlerSuccess() throws InterruptedException {
        testRuleHandler(2);
        assertTrue(rulesResult);
    }

    @Test
    public void testRuleHandlerFail() throws InterruptedException {
        testRuleHandler(1);
        assertFalse(rulesResult);
    }

    private void testRuleHandler(final int instanceCount) throws InterruptedException {
        //Mock AutoScaling Group
        AutoScalingGroup autoScalingGroup = mock(AutoScalingGroup.class);
        when(autoScalingGroup.getAutoScalingGroupName()).thenReturn(testAutoScalingGroupName);
        //Mock instances in auto scale group
        Instance instance = mock(Instance.class);
        List<Instance> instanceList = mock(List.class);
        instanceList.add(instance);
        when(autoScalingGroup.getInstances()).thenReturn(instanceList);
        when(autoScalingGroup.getInstances().size()).thenReturn(instanceCount);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        ruleHandler.applyRules(autoScalingGroup, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(
                @Nullable
                Boolean result) {
                rulesResult = result;
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
    }


}
