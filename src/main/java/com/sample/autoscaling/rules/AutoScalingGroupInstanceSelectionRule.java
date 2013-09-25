package com.sample.autoscaling.rules;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;

/**
 * Interface representing "rule" that needs to be applied by program when selecting an instance in an auto scaling group
 * for termination.
 */
public interface AutoScalingGroupInstanceSelectionRule {

    /**
     * Rule Handler will call this method on all the configured rules. if any rule fails, it will return false.
     *
     * @param autoScalingGroup
     *
     * @return false if any rule fails, otherwise true.
     */
    public boolean apply(AutoScalingGroup autoScalingGroup);

}
