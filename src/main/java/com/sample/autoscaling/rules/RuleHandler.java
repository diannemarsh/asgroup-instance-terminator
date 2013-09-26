package com.sample.autoscaling.rules;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RuleHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleHandler.class);

    @Autowired
    private List<AutoScalingGroupInstanceSelectionRule> instanceSelectionRules;

    /**
     * @Resource is required because of Property Editors magic, see ListOperationsEditor
     */
    @Resource(name = "redisTemplate")
    private HashOperations<String, Long, String> hashOps;

    /**
     * This method will apply all the rules on auto scaling group. All the rules are ordered. If any of the rules fails,
     * further processing of rules will be skipped and none of the instances will be terminated in the auto scaling
     * group during current job run.
     *
     * @param jobSequenceNumber - Job Sequence Number of this run
     * @param autoScalingGroup - Auto Scaling group on which rules are running
     * @param countDownLatch - Count Down latch that needs to be counted down when auto scaling group is procesed.
     */
    @Async
    public void applyRules(Long jobSequenceNumber, AutoScalingGroup autoScalingGroup, CountDownLatch countDownLatch) {
        boolean ruleExecutionStatus = false;
        try {
            for (AutoScalingGroupInstanceSelectionRule rule : instanceSelectionRules) {
                ruleExecutionStatus = rule.apply(autoScalingGroup);
                if (!ruleExecutionStatus) {
                    RuleDescription ruleDescription =
                        AnnotationUtils.findAnnotation(rule.getClass(), RuleDescription.class);
                    LOGGER.info("Rule - {} {} failed", ruleDescription != null ? ruleDescription.value() : "",
                        autoScalingGroup.getAutoScalingGroupName());
                    hashOps.put(autoScalingGroup.getAutoScalingGroupName(), jobSequenceNumber,
                        "FAILED:" + ruleDescription.value());
                    //If one rule fails, there is no need to process further.
                    break;
                }
            }
            //if all the rules passed terminate a random instance in auto scaling group
            if (ruleExecutionStatus) {
                terminateInstance(jobSequenceNumber, autoScalingGroup);
            }
        }
        catch (Exception ex) {
            hashOps.put(autoScalingGroup.getAutoScalingGroupName(), jobSequenceNumber, "FAILED:" + ex.getCause());
        }
        finally {
            countDownLatch.countDown();
        }
    }

    /**
     * This method will terminate the instance in auto scaling group. It will not try to apply any rules to pick the
     * instance. It will completely depend on rules applied by auto scaling termination process. This method will also
     * be executed in callback thread, not the scheduler thread.
     *
     * @param jobSequenceNumber
     * @param autoScalingGroup
     */
    private void terminateInstance(Long jobSequenceNumber, AutoScalingGroup autoScalingGroup) {
        LOGGER.info("All rules passed for auto scaling group {}, terminating an instance in the group",
            autoScalingGroup.getAutoScalingGroupName());
        hashOps.put(autoScalingGroup.getAutoScalingGroupName(), jobSequenceNumber, "Terminated Instance:X");
    }

    /**
     * Sort the rules before processing so that rules are executed in defined order. Each rule is annotated with order
     * annotation.
     */
    @PostConstruct
    public void afterPropertiesSet() {
        AnnotationAwareOrderComparator.sort(instanceSelectionRules);
    }
}
