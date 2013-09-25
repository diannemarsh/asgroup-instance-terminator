package com.sample.autoscaling.rules;

import java.util.List;

import com.amazonaws.services.autoscaling.AmazonAutoScalingAsync;
import com.amazonaws.services.autoscaling.model.Activity;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeScalingActivitiesRequest;
import com.amazonaws.services.autoscaling.model.DescribeScalingActivitiesResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Rule to make sure that there is no auto scaling (scale up) activity in last 10 minutes. If there is any scale up
 * activity in last 10 minutes, auto scaling group will be by-passed in this run.
 */
@Order(2)
@Component
public class AutoScalingGroupActivityRule implements AutoScalingGroupInstanceSelectionRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScalingGroupActivityRule.class);

    @Autowired
    private AmazonAutoScalingAsync autoScalingClient;

    @Override
    public boolean apply(AutoScalingGroup autoScalingGroup) {
        //Build a request to describe auto scaling group activities.
        DescribeScalingActivitiesRequest scalingActivitiesRequest =
            new DescribeScalingActivitiesRequest().withAutoScalingGroupName(autoScalingGroup.getAutoScalingGroupName())
                .withMaxRecords(1);
        // Get the auto scaling group activities
        DescribeScalingActivitiesResult scalingActivitiesResult =
            autoScalingClient.describeScalingActivities(scalingActivitiesRequest);
        List<Activity> scalingActivities = scalingActivitiesResult.getActivities();
        LOGGER.info("Scaling Activities of Auto-Scaling group {} are {}", autoScalingGroup.getAutoScalingGroupName(),
            scalingActivities);
        return scalingActivities.size() > 0;
    }

}
