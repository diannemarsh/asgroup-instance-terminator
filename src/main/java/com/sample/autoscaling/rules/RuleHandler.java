package com.sample.autoscaling.rules;

import java.util.List;

import javax.annotation.PostConstruct;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.google.common.util.concurrent.FutureCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RuleHandler {

    @Autowired
    private List<AutoScalingGroupInstanceSelectionRule> instanceSelectionRules;

    /**
     * This method will apply all the rules on auto scaling group. All the rules are ordered. If any of the rules fails,
     * further processing of rules will be skipped and none of the instances will be terminated in the auto scaling
     * group during current job run.
     *
     * @param autoScalingGroup
     * @param callback
     */
    @Async
    public void applyRules(AutoScalingGroup autoScalingGroup, FutureCallback<Boolean> callback) {
        Boolean status = false;
        try {
            for (AutoScalingGroupInstanceSelectionRule rule : instanceSelectionRules) {
                status = rule.apply(autoScalingGroup);
                if (!status) {
                    //If one rule fails, there is no need to process further.
                    break;
                }
            }
        }
        catch (Exception ex) {
            callback.onFailure(ex);
        }
        callback.onSuccess(status);
    }

    /**
     * Sort the rules before processing so that rules are executed in defined order. Each rule is annotated with order
     * annotation.
     */
    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        AnnotationAwareOrderComparator.sort(instanceSelectionRules);
    }
}
