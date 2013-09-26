asgroup-instance-terminator
===========================

This project will schedule a job that will try to kill an instance of every auto scaling group in an AWS account at regular intervals.
Schedule of this job can be configured in a property file. This job will run following rules before terminating an instance in auto scaling group.

1. Auto Scaling group should have more than one instance in auto scaling group.
2. There should not be any scale up activity in last 30 minutes.

If any of the above condition fails for an auto scaling group, Job will skip the auto scaling group for that run.

How to run
===========================

Step 1: Clone this repository
Step 2: Configure AWS Credentials
Open file asgroup-instance-terminator/src/main/resources/AwsCredentials.properties and provide your aws account access and secret key
Step 3: For Unit tests to be successful
Open file asgroup-instance-terminator/src/test/resources/sample-application-test.properties and provide a test auto scaling group name present
in your account.
Step 4: mvn clean install
Step 5: Run the application java -jar asgroup-instance-terminator-1.0.0-SNAPSHOT.jar 
(Logs will be generated in a file called app.log in the same directory from where you run the job)

What can be configured
===========================
Job Schedule can be configured in file asgroup-instance-terminator/src/main/resources/sample-application.properties by changing following property
cron.job.schedule
