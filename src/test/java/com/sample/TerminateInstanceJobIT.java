package com.sample;

import static junit.framework.Assert.assertNotNull;

import java.util.List;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.sample.autoscaling.config.Config;
import com.sample.autoscaling.job.TerminateInstanceJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = Config.class)
public class TerminateInstanceJobIT {

    @Autowired
    private TerminateInstanceJob terminateInstanceJob;

    @Test
    public void testApplicationContext() {
        //Test to make sure application context is loading properly.
    }

    @Test
    public void testGetAutoScalingGroups() {
        List<AutoScalingGroup> autoScalingGroupList = terminateInstanceJob.getAllAutoScalingGroups();
        assertNotNull(autoScalingGroupList);
    }


}
