package com.sample.autoscaling.rules;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Rule to make sure that there are more than once instance in auto scaling group. Program can not terminate one and
 * only instance in an auto scaling group.
 */
@Order(1)
@Component
public class AutoScalingGroupInstancesCountRule implements AutoScalingGroupInstanceSelectionRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScalingGroupInstancesCountRule.class);

    @Value("${auto.termination.minimum.instance.threshold}")
    private int minThresholdForInstanceTermination;

    @Override
    public boolean apply(AutoScalingGroup autoScalingGroup) {
        LOGGER.debug("Number of instances in auto-scaling group {} are: {}", autoScalingGroup.getAutoScalingGroupName(),
            autoScalingGroup.getInstances().size());
        return autoScalingGroup.getInstances().size() > minThresholdForInstanceTermination;
    }

}
