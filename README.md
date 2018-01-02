# HTTPLoadTest-baseline

A typical simple baseline for building LoadTest for continous deploy/continous production QA pipelines


![Build Status](https://jenkins.capraconsulting.no/buildStatus/icon?job=Cantara-HTTPLoadTest-baseline) - [![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](http://www.repostatus.org/badges/latest/active.svg)](http://www.repostatus.org/#active) 

[![Known Vulnerabilities](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline/badge.svg)](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline)


![Ugly UI whiteboard mockup](https://raw.githubusercontent.com/Cantara/HTTPLoadTest-Baseline/master/whiteboard-UI-config-mockup.jpg)


# Example LoadTestConfig
```json
{
  "test_id": "MyTestID",
  "test_name": "An example of load test configuration",
  "test_no_of_threads": 10,
  "test_read_write_ratio": 90,
  "test_sleep_in_ms": 10,
  "test_randomize_sleeptime": true,
  "test_duration_in_seconds": 20
}
```

# Example on load test result
```text
08:38:12.202 [main] INFO  n.c.s.loadtest.LoadTestResource -  98 read tests resulted in 42 successfull runs where 6 was marked as deviations.
08:38:12.202 [main] INFO  n.c.s.loadtest.LoadTestResource -  12 write tests resulted in 3 successfull runs where 1 was marked as deviations.
08:38:12.202 [main] INFO  n.c.s.loadtest.LoadTestResource -  0 unmarked tests resulted in 0 successfull runs where 0 was marked as deviations.
08:38:12.202 [main] INFO  n.c.s.loadtest.LoadTestResource -  110 total tests resulted in 45 successfull runs where 7 was marked as deviations.

```

# Example on test invocation result
```json
{
  "test_id" : "r-MyTestID",
  "test_name" : "An example of load test configuration",
  "test_tags" : "URL: http://bing.com/",
  "test_run_no" : 43,
  "test_duration" : 429,
  "test_success" : true,
  "test_deviation_flag" : false,
  "test_timestamp" : 1514878656855
}
```


Quick build and verify
'''
* mvn clean install
* java -jar target/HTTPLoadTest-baseline-0.1-SNAPSHOT.jar
* wget http://localhost:8086/HTTPLoadTest-baseline/health
* wget http://localhost:8086/HTTPLoadTest-baseline/config                      // UI to configure a loadTest Run
* wget http://localhost:8086/HTTPLoadTest-baseline/loadTest                    // return all loadTests with statuses
* wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/loadtestId/status  // return status for loadtest with loadTestid
'''

Open in browser:  
* http://localhost:8086/HTTPLoadTest-baseline/config
