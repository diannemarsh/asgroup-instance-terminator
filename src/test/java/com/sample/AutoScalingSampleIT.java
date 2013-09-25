package com.sample;

import com.sample.autoscaling.config.Config;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = Config.class)
public class AutoScalingSampleIT {

    @Test
    public void testAutoScalingGroups() throws InterruptedException {
        Thread.sleep(15000);
    }

}
