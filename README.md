# HTTPLoadTest-baseline

A simple baseline for building LoadTests to be used for continous deploy/continous production QA pipelines.  Baseline projects are meant to be a git clone starting 
point for for software which are expected to grow and flourish in different ways which are not easy to parameterize in early stages. It should be usable for quite 
a few settings, but is expected to grow in different directions. We would love to receive pull-request for enhancements both on current codebase and extensibillity features.


![Build Status](https://jenkins.capraconsulting.no/buildStatus/icon?job=Cantara-HTTPLoadTest-baseline) - [![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](http://www.repostatus.org/badges/latest/active.svg)](http://www.repostatus.org/#active) 

[![Known Vulnerabilities](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline/badge.svg)](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline)


# Quick test-run in docker

```
sudo docker run -d -p 28086:8086  cantara/httploadtest-baseline
wget http://localhost:28086/HTTPLoadTest-baseline/health
```
Open in browser:  
* To configure and start a load test: http://localhost:28086/HTTPLoadTest-baseline/config   



# Pipeline usage example

```
wget -post-data "jsonConfig=@loadTestReadSpecification.json http://localhost:28086/HTTPLoadTest-baseline/loadTest/read"
wget -post-data "jsonConfig=@loadTestWriteSpecification.json http://localhost:28086/HTTPLoadTest-baseline/loadTest/write"
wget -post-data "jsonConfig=@loadTestConfig.json http://localhost:28086/HTTPLoadTest-baseline/loadTest"
##  wait and get the result
sleep 40s
wget -o result.txt http://localhost:8086/HTTPLoadTest-baseline/loadTest/status
## check the results.txt against QA rules
```


# Work-in-progress - nice vizualisation of running loadtests

![Ugly UI whiteboard mockup](https://raw.githubusercontent.com/Cantara/HTTPLoadTest-Baseline/master/whiteboard-UI-config-mockup.jpg)


### Example LoadTestConfig
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

## Example on load test result
```text
Started: 02/01-2018  14:42:50  Now: 02/01-2018  14:43:02  Running for 117 seconds.

  203 read tests resulted in 202 successful runs where 1 was marked as deviation(s).
   22 write tests resulted in 22 successful runs where 0 was marked as deviation(s).
    0 unmarked tests resulted in 0 successful runs where 0 was marked as deviation(s).
  225 total tests resulted in 224 successful runs where 1 was marked as deviation(s).

```

### Example on test invocation result
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

## Example on read test specification
```json
[ {
  "command_url" : "https://gmail.com",
  "command_contenttype" : "text/html",
  "command_http_post" : false,
  "command_timeout_milliseconds" : 5000,
  "command_template" : "",
  "command_replacement_map" : {
    "#Passord" : "TestPassord",
    "#BrukerID" : "TestBruker"
  }
  },
  {
  "command_url" : "http://test.tull.no",
  "command_contenttype" : "application/json",
  "command_http_post" : true,
  "command_timeout_milliseconds" : 5000,
  "command_template" : "{\n  \\\"sub\\\": \\\"#fizzle(digits:67643)\\\",\n  \\\"name\\\": \\\"#BrukerID\\\",\n  \\\"admin\\\": true\n}",
  "command_replacement_map" : {
    "#Passord" : "TestPassord",
    "#BrukerID" : "TestBruker"
  },
   "command_response_map" : {
     "#randomizeName" : "$..test_randomize_sleeptime",
      "#testName" : "$..test_name"
  }
} ]
```

#### Template special operations

```
#fizzle(chars:replaceMe)                          =>  tEftohTdS
#fizzle(digits:67643)                             =>  32632
#fizzle(U_chars:(TEST)                            =>  WRVY
#fizzle(L_chars:(lower)                           =>  tgewt
#fizzle(HEX:(a hexvalue)                          =>  4E7AD3B084
 #fizzle(option:yes, no, here, there)             =>  here
```

### Example on write test specification
```json

[ {
  "command_url" : "https://gmail.com",
  "command_contenttype" : "text/html",
  "command_http_post" : false,
  "command_timeout_milliseconds" : 5000,
  "command_template" : "",
  "command_replacement_map" : {
    "#Passord" : "TestPassord",
    "#BrukerID" : "TestBruker"
  }
}, {
  "command_url" : "http://test.tull.no",
  "command_contenttype" : "application/json",
  "command_http_post" : true,
  "command_timeout_milliseconds" : 5000,
  "command_template" : "{\n  \\\"sub\\\": \\\"#fizzle(chars:replaceMe)\\\",\n  \\\"name\\\": \\\"#BrukerID\\\",\n  \\\"admin\\\": true\n}",
  "command_replacement_map" : {
    "#Passord" : "TestPassord",
    "#BrukerID" : "TestBruker"
  },
   "command_response_map" : {
     "#randomizeName" : "$..test_randomize_sleeptime",
     "#testName" : "$..test_name"
  }
} ]
```


#### If you want to provision several sets of TestSpecifications, you can add something like this to ./config_override/application_override.properties
```properties
TestSpecification.1.read.filename=./specifications/FirstReadTestSpecification.json
TestSpecification.1.write.filename=./specifications/FirstWriteTestSpecification.json
TestSpecification.2.read.filename=./specifications/SecondReadTestSpecification.json
TestSpecification.2.write.filename=./specifications/SecondWriteTestSpecification.json
```

# Development and Contribution

### Quick build and verify
```
mvn clean install
java -jar target/HTTPLoadTest-baseline-0.1-SNAPSHOT.jar
wget http://localhost:8086/HTTPLoadTest-baseline/health
wget http://localhost:8086/HTTPLoadTest-baseline/config                      // UI to configure a loadTest Run
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest                    // return all loadTests with statuses
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/loadtestId/status  // return status for loadtest with loadTestid
```

Open in browser:  
* To configure and start a load test: http://localhost:8086/HTTPLoadTest-baseline/config   
* To configure the read-testdriver: http://localhost:8086/HTTPLoadTest-baseline/config/read   
* To configure the write-testdriver: http://localhost:8086/HTTPLoadTest-baseline/config/write   
* To select between pre-configured TestSpecificationsr: http://localhost:8086/HTTPLoadTest-baseline/config/select   
