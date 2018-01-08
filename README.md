# HTTPLoadTest-baseline

##### Status

![Build Status](https://jenkins.capraconsulting.no/buildStatus/icon?job=Cantara-HTTPLoadTest-baseline) - [![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](http://www.repostatus.org/badges/latest/active.svg)](http://www.repostatus.org/#active) 

[![Known Vulnerabilities](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline/badge.svg)](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline)


A simple baseline for building LoadTests to be used for continous deploy/continous production QA pipelines.  Baseline projects are meant to be a git clone starting point for for software which are expected to grow and flourish in different ways which are not easy to parameterize in early stages. It should be useable for quite a few settings, but is expected to grow in different directions. We would love to receive pull-request for enhancements both on current codebase and extensibillity features.

Why another load-test OpenSource project?  We think this is a reasonable question. There exist quite a few "full-fledged" alternatives. Our concern is that the uptake of those solutions are way below what the industry need, and this is an attempt to try to offer a different alternative which may or may not fulfill you needs/requirements. 

Coming from development backgrounds, we hope that a baseline you might contribute to, or just form and change to your requirements/needs might increase the quality of produces software by making it less "expensive" to add this type of quality processes into your software development process.

#### The process-flow of load-testing

![The flow of LoadTest investments](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/HTTPLoadTest-FullProcessFlow.png)

#### A quick intro/test-run

```jshelllanguage
sudo docker run -d -p 28086:8086  cantara/httploadtest-baseline
wget http://localhost:28086/HTTPLoadTest-baseline/health
```
Open in browser:  
* To configure and start a load test: http://localhost:28086/HTTPLoadTest-baseline/config   



#### The flow of TestSpecification Execution

![The flow of TestSpecification Execution](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/TestSpecificationExecutionFlow.png)

### Example-oriented documentation to get familiar with the application

Since the main goal for this codebase is to simplify Companies efforts in ensuring that a agile or continous investment into software development does not compromise the quality assurance processes on non-functional requirements, we have tried to focus on making the load-test QA process easily embeddable to a Company's contonous CI/CD processes. This means that the application have two modi:  a) exploration modi - where you (as in any normal QA åprocess, can analyzy the load-characteristics of your application and b) easily add the baseline characteristics to the deployment QA pipelines.

Let's have a look at the details...

### Pipeline usage example

```jshelllanguage
wget -post-data "jsonConfig=@loadTestReadSpecification.json http://localhost:28086/HTTPLoadTest-baseline/loadTest/read"
wget -post-data "jsonConfig=@loadTestWriteSpecification.json http://localhost:28086/HTTPLoadTest-baseline/loadTest/write"
wget -post-data "jsonConfig=@loadTestConfig.json http://localhost:28086/HTTPLoadTest-baseline/loadTest"
##  wait and get the result
sleep 40s
wget -o result.txt http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus
## check the results.txt against QA rules
```



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

The LoadTestConfig is the configuration of the top-level parameters of a load test and control the behaviour of the two (read/write) TestSpesifications
(see further down for more information and examples of how to configure TestSpecifications). The LoadTestConfig also consist of the load-time, which is 
the longest allowed time the tests are allowed to run.
 


## Example on load test result
```text
{ "HTTPLoadTest-status": 
"Started: 08/01-2018  18:31:56  Now: 08/01-2018  18:32:10  Ran for 08/01-2018  18:32:06 seconds.

  101 read tests resulted in 101 successful runs where 0 was marked failure and 0 was marked as deviation(s).
    8 write tests resulted in 6 successful runs where 2 was marked failure and 0 was marked as deviation(s).
    0 unmarked tests resulted in 0 successful runs where 0 was marked failure and  0 was marked as deviation(s).
  109 total tests resulted in 109 successful runs where 0 was marked failure and 0 was marked as deviation(s).
   88 active tests threads scheduled, number of threads configured: 10,  isRunning: false 
  928 ms mean duraction for successful read tests, 1286 ms ninety percentile successful read tests 
 11801 ms mean duraction for successful write tests, 1762 ms ninety percentile successful write tests 

{
  "test_id" : "Default-1234",
  "test_name" : "Default values for LoadTest Configuration",
  "test_no_of_threads" : 10,
  "test_read_write_ratio" : 90,
  "test_sleep_in_ms" : 50,
  "test_randomize_sleeptime" : true,
  "test_duration_in_seconds" : 10
}```

If we look at the example output above, we see that we have a load-test with a read/write ratio of 90, 
which mean that we have almost 10 times more runs of the read TestConfiguration than we have invocations of the write TestSpecification. 
In this run, we have recorded one failed read run, which was a result of a timeout. (If we got non 2xx HTTP codes, the result would be failed test.)


### Example on test invocation result
```json
{
  "test_id" : "r-MyTestID",
  "test_name" : "An example of load test configuration",
  "test_tags" : "URL: http://bing.com/",
  "test_run_no" : 43,
  "test_duration" : 429,
  "test_success" : true,
  "test_deviation_flag" : true,
  "test_timestamp" : 1514878656855
}
```

This example show that we have received an HTTP 2xx code, but something unexpected happened and the response was marked as an deviation. The most 
common cause of deviations in HTTPLoadTest is timeout, as we use an underlaying icuit-breaker framework called hystrix to avoid blocking and dangeling 
HTTP requests and internal threads.



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
  "command_url" : "http://test.tull.no/#MySite",
  "command_contenttype" : "application/json",
  "command_http_authstring": "Basic ZGV2ZWxvcG1lbnQtbWVkaWNhdGlvbjoxT09jTEpoR01oSyMxMHUhRDMzUw==",
  "command_http_post" : true,
  "command_timeout_milliseconds" : 5000,
  "command_template" : "{\n  \\\"sub\\\": \\\"#fizzle(digits:67643)\\\",\n  \\\"name\\\": \\\"#BrukerID\\\",\n  \\\"admin\\\": true\n}",
  "command_replacement_map" : {
    "#Passord" : "TestPassord",
    "#BrukerID" : "TestBruker",
    "#MySite" : "demo"
  },
   "command_response_map" : {
     "#randomizeName" : "$..test_randomize_sleeptime",
      "#testName" : "$..test_name"
  }
} ]
```
From the example above, we can see use of several of HTTPLoadTest features. The command_replacement_map is a supplied variable-set for the given command. This is used
to replace parts of an URL or parts of an HTTP payload. The command_response_map consist of JsonPath expression which is matched against the response and put into the
corresponding named variable. These gererated variables can then be used in consequitive commands alond with the variables in the command_replacement_map. If you need 
a list as a result, you might find the #fizzle(option:#testName) feature handy. This will pick a random value from the returned response and substitute it in the next
command.  As of now, only simple, pre-calculated http badic-auth is supported.









#### Template special operations

```jshelllanguage
#fizzle(chars:replaceMe)                          =>  tEftohTdS
#fizzle(digits:67643)                             =>  32632
#fizzle(U_chars:(TEST)                            =>  WRVY
#fizzle(L_chars:(lower)                           =>  tgewt
#fizzle(HEX:(a hexvalue)                          =>  4E7AD3B084
#fizzle(option:yes, no, here, there)              =>  here
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
    "#BrukerID" : "User_#fizzle(HEX:3234)"
  }
}, {
  "command_url" : "http://test.tull.no",
  "command_contenttype" : "application/json",
  "command_http_post" : true,
  "command_http_authstring": "username/password",
  "command_timeout_milliseconds" : 5000,
  "command_template" : "FILE:./templates/my_test_tull_template.json",
  "command_replacement_map" : {
    "#Simulate" : "TestPassord",
    "#Name" : "#fizzle(option:Steve, Kate, Simon)"
  },
   "command_response_map" : {
     "#randomizeName" : "$..test_randomize_sleeptime",
     "#testName" : "$..test_name"
  }
} ]
```

In this example, we have included an example where we create a randomized BrukerID variable for each TestSpecification invocation to simulate
run with lots of different users.


#### If you want to provision several sets of TestSpecifications, you can add something like this to ./config_override/application_override.properties
```properties
TestSpecification.1.read.filename=./specifications/FirstReadTestSpecification.json
TestSpecification.1.write.filename=./specifications/FirstWriteTestSpecification.json
TestSpecification.2.read.filename=./specifications/SecondReadTestSpecification.json
TestSpecification.2.write.filename=./specifications/SecondWriteTestSpecification.json
```

# Protecting the LoadTest WebUI

If you want to add basic authentication to HTTLLoadTest, just add the following to config_override/application_override.properties
```properties
loadtest.basicauth=true

login.admin.user=admin
login.admin.password=adminservice
```


# Development and Contribution

### Quick build and verify

```jshelllanguage
mvn clean install
java -jar target/HTTPLoadTest-baseline-0.9.2-SNAPSHOT.jar
wget http://localhost:8086/HTTPLoadTest-baseline/health
wget http://localhost:8086/HTTPLoadTest-baseline/config                      // UI to configure a loadTest Run
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/status             // return latest loadTests with status
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/stop               // stop all loadTests
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus         // return alle the loadTest results in json
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus_csv     // return alle the loadTest results in csv
```

Open in browser:  
* To configure and start a load test: http://localhost:8086/HTTPLoadTest-baseline/config   
* To configure the read-testdriver: http://localhost:8086/HTTPLoadTest-baseline/config/read   
* To configure the write-testdriver: http://localhost:8086/HTTPLoadTest-baseline/config/write   
* To select between pre-configured TestSpecificationsr: http://localhost:8086/HTTPLoadTest-baseline/config/select


### Work-in-progress - nice vizualisation of running loadtests

Since the main use meant for HTTPLoadTest is in automated QA pipelines, we have initially not spent much effort in 
nice WebUI and result vizualizations. We hope to address this shortcoming some time in the future and will be glad 
for any helping hands.

![Ugly UI whiteboard mockup](https://raw.githubusercontent.com/Cantara/HTTPLoadTest-Baseline/master/whiteboard-UI-config-mockup.jpg)


### Troubleshoting   

*Maven fails*
```jshelllanguage
> mvn -version
Requires Java 8
> export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
> mvn clean install
```
