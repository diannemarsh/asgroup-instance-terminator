package com.sample;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring Test configuration file. Purpose is to load test properties file.
 */
@Configuration
@PropertySource("classpath:sample-application-test.properties")
public class TestConfig {

}
