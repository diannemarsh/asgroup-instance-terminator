package com.sample.autoscaling.rules;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;

/**
 * Interface representing "Instance selection rule" that needs to be applied by program when selecting an instance in an
 * auto scaling group for termination.
 */
public interface AutoScalingGroupInstanceSelectionRule {

    public boolean apply(AutoScalingGroup autoScalingGroup);

}
