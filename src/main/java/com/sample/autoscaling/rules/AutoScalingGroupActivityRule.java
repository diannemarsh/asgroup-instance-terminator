package com.sample.autoscaling.rules;

import java.util.List;

import com.amazonaws.services.autoscaling.AmazonAutoScalingAsync;
import com.amazonaws.services.autoscaling.model.Activity;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeScalingActivitiesRequest;
import com.amazonaws.services.autoscaling.model.DescribeScalingActivitiesResult;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Rule to make sure that there is no scale up activity in progress or in last n minutes (n is configurable). If there
 * is any scale up activity in last n minutes or is in progress, auto scaling group will be by-passed in this run.
 */
@Order(2)
@Component
public class AutoScalingGroupActivityRule implements AutoScalingGroupInstanceSelectionRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScalingGroupActivityRule.class);

    @Value("${auto.termination.minimum.scaling.activity.threshold}")
    private int minThresholdForLastScalingActivity;

    @Autowired
    private AmazonAutoScalingAsync autoScalingClient;

    @Override
    public boolean apply(AutoScalingGroup autoScalingGroup) {
        //Build a request to describe auto scaling group activities.
        DescribeScalingActivitiesRequest scalingActivitiesRequest =
            new DescribeScalingActivitiesRequest().withAutoScalingGroupName(autoScalingGroup.getAutoScalingGroupName())
                .withMaxRecords(1);//Latest Activity Only

        // Get the auto scaling group activities. Only activities from the past six weeks are returned. Activities
        // still in progress appear first on the list.
        DescribeScalingActivitiesResult scalingActivitiesResult =
            autoScalingClient.describeScalingActivities(scalingActivitiesRequest);
        List<Activity> scalingActivities = scalingActivitiesResult.getActivities();
        LOGGER.debug("Scaling Activities of Auto-Scaling group {} are {}", autoScalingGroup.getAutoScalingGroupName(),
            scalingActivities);

        //If there is no activity in last six week, instance of this auto scaling group can be terminated.
        boolean status = scalingActivities.size() == 0;
        // If there is scaling activity
        if (scalingActivities.size() > 0) {
            //Get the most recent activity
            Activity latestScalingActivity = scalingActivities.get(0);
            status = !(isScaleUpActivityInProgress(latestScalingActivity) ||
                isScaleUpActivityCompletedRecently(latestScalingActivity));
        }

        return status;
    }

    /**
     * Check if the scale up activity is in progress.
     *
     * @param latestScalingActivity - Latest Scaling Activity
     *
     * @return True if Activity in progress is scale up.
     */
    private boolean isScaleUpActivityInProgress(Activity latestScalingActivity) {
        Integer progress = latestScalingActivity.getProgress();
        //If the scaling activity is in progress and it is scale up
        //TODO Find a better way than depending on description
        return progress < 100 && latestScalingActivity.getDescription().startsWith("Launching");
    }


    /**
     * Check if there is scale up activity in last n minutes.
     *
     * @param latestScalingActivity - Latest Scaling Activity
     *
     * @return True if there is scale up activity in last n minutes.
     */
    private boolean isScaleUpActivityCompletedRecently(Activity latestScalingActivity) {
        int minutesSinceLastCompletedActivity =
            Minutes.minutesBetween(new DateTime(latestScalingActivity.getEndTime()), DateTime.now()).getMinutes();
        return minutesSinceLastCompletedActivity <= minThresholdForLastScalingActivity &&
            latestScalingActivity.getDescription().startsWith("Launching");
    }

}
