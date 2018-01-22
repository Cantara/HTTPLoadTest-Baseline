# HTTPLoadTest-Baseline

##### Status
![Build Status](https://jenkins.capraconsulting.no/buildStatus/icon?job=Cantara-HTTPLoadTest-baseline) - 
[![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](http://www.repostatus.org/badges/latest/active.svg)](http://www.repostatus.org/#active)    [![Known Vulnerabilities](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline/badge.svg)](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline)


HTTPLoadTest-Baseline is simple starting point for building LoadTests to be used for continuous deploy/continuous production
QA pipelines. Baseline projects are meant to be a git clone starting point for for software which are expected to grow and
flourish in different ways which are not easy to parameterize in early stages. It should be usable for quite a few settings,
but is expected to grow in different directions. We would love to receive pull-request for enhancements both on current
codebase and extensibility features.

Why another load-test OpenSource project?  We think this is a reasonable question. There exist quite a few "full-fledged"
alternatives. Our concern is that the uptake of those solutions are way below what the industry need, and this is an attempt to try to offer a different alternative which may or may not fulfill you needs/requirements.  The main goal for this codebase
is to simplify Companies efforts in ensuring that an agile or continuous investment into software development does not
compromise the quality assurance processes on non-functional requirements, we have tried to focus on making the load-test 
QA process easily embedable to a Company's continuous CI/CD processes.

We have built HTTPLoadTest-Baseline on an underlaying cicuit-breaker framework called hystrix and timed execution blocks to
avoid any blocking or dangeling HTTP requests and internal threads.

Coming from development backgrounds, we hope that a baseline you might contribute to, or just form and change to your
requirements/needs might increase the quality of produces software by making it less "expensive" to add this type of 
quality processes into your software development process.

#### The process-flow of load-testing

![The flow of LoadTest investments](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/images/HTTPLoadTest-FullProcessFlow.png)

#### A quick intro/test-run

```jshelllanguage
sudo docker run -d -p 28086:8086  cantara/httploadtest-baseline
wget http://localhost:28086/HTTPLoadTest-baseline/health
```
Then open the simple UI in a web browser:  
* To configure and start a load test: http://localhost:28086/HTTPLoadTest-baseline/config   


### Example-oriented documentation to get familiar with the application

Since the main goal for this codebase is to simplify Companies efforts in ensuring that an agile or continous investment into
software development does not compromise the quality assurance processes on non-functional requirements, we have tried to
focus on making the load-test QA process easily embeddable to a Company's continous CI/CD processes. This means that the
application have two modi:  
* a) an exploration modi - where you (as in any normal QA process, can analyze the load-characteristics of your application
(load-limits, behaviour with excessive load, endurance charasteristics under high load and the like) 
* b) easily add the baseline characteristics as benchmarks to the deployment QA pipelines and detect when changes break the
expectations.

So, let us have a look at the details...

### HTTPLoadTest, used as pipeline example

```jshelllanguage
JSON=$(<ReadTestSpecification-TestHealth.json ); wget -v --post-data "jsonConfig=${JSON}" -X POST  http://localhost:28086/HTTPLoadTest-baseline/loadTest/form/read
JSON=$(<WriteTestSpecification_TestOauth2ProtectedResource.json ); wget -v --post-data "jsonConfig=${JSON}" -X POST  http://localhost:28086/HTTPLoadTest-baseline/loadTest/form/write
JSON=$(<LoadTestBenchmark.json ); wget -v --post-data "jsonConfig=${JSON}" -X POST  http://localhost:28086/HTTPLoadTest-baseline/loadTest/form/benchmark
JSON=$(<LoadTestConfig.json ); wget -v --post-data "jsonConfig=${JSON}" -X POST  http://localhost:28086/HTTPLoadTest-baseline/loadTest/form
##  wait until loadtest is complete
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:28086/HTTPLoadTest-baseline/loadTest/runstatus)" == "409" ]]; do sleep 5; done
## check the results.txt against QA rules
wget  --content-on-error http://localhost:28086/HTTPLoadTest-baseline/loadTest/runstatus
## To download results from earlier test-runs, use /health to find the name of the test-run, and
cat runstatus
```

* [See also HTTPLoadTest-QAFlow-Demo for more details.](https://github.com/Cantara/HTTPLoadTest-Baseline/blob/master/scripts/HTTPLoadTest-QAFlow-Demo/HTTPLoadTest%20-QAFlow-Demo.md)



### Example LoadTestConfig
```json
{
  "test_id": "MyTestID",
  "test_name": "An example of load test configuration",
  "test_no_of_threads": 10,
  "test_read_write_ratio": 90,
  "test_sleep_in_ms": 10,
  "test_randomize_sleeptime": true,
  "test_duration_in_seconds": 20,
  "test_global_variables_map": {
    "#Password": "MyTestPasseord",
    "#UserID": "MyTestUser"
  }
}
```

The LoadTestConfig is the configuration of the top-level parameters of a load test and control the behaviour of the two
(read/write) TestSpesifications (see further down for more information and examples of how to configure TestSpecifications).
The LoadTestConfig also consist of the load-time, which is the longest allowed time the tests are allowed to run.  You can
define LoadTest global variables for each LoadTestConfig.
 


## Example on load test result
```text
{ "HTTPLoadTest-health": "OK", 
"version": "(DEV VERSION)", 
"now":"2018-01-15T10:39:37.676Z", 
"running since": "2018-01-15T10:38:47.334Z", 

"resulfiles": 
  "HTTPLoadTest-Health_1516012005749.json, HTTPLOadTest-Health_1516003617097.csv, HTTPLoadTest-Health_1516008000667.json, HTTPLoadTest-Health_1516012005749.csv", 

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
  "test_duration_in_seconds" : 10,,
  "test_global_variables_map": {
     "#Password": "MyTestPasseord",
     "#UserID": "MyTestUser"
  }
}
```

If we look at the example output above, we see that we have a load-test with a read/write ratio of 90, which mean that we
have almost 10 times more runs of the read TestConfiguration than we have invocations of the write TestSpecification. In 
this run, we have recorded one failed read run, which was a result of a timeout. (If we got non 2xx HTTP codes, the result
would be failed test.)


Note: The test_read_write_ratio has to be between 1 (%) and 100 (%) as of now, since we have some special-handling for values
outside that range. If you want to experience running a single test at a time, you can add the same testSpecification as both
read-TestSpecification and write-TestSpecification to archieve the same result.


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

This example show that we have received an HTTP 2xx code, but something unexpected happened and the response was marked as an
deviation. The most common cause of deviations in HTTPLoadTest is timeout, as we use an underlaying cicuit-breaker framework
called hystrix to avoid blocking and dangeling HTTP requests and internal threads.


## Example on read test specification

#### The flow of TestSpecification Execution

![The flow of TestSpecification Execution](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/images/TestSpecificationExecutionFlow.png)

The diagram try to show how a TestSpecification consists of a chain of Comands(HTTP-requests), which all report to
TestResult. The commands use a variable-map to replace tags/markers in the command-specification for URL and template to give
you flexibility to control parameters++. The result (HTTP-response) is parsed with the instructions in the
command_response_map and the value of this parsing is put into the named variable in the same map. These calculated variables
is then added to the command_replacement_map to the next command. The next command also inherit the map from the previos step
to ensure you have the same variable from thinks like options/set selections.


```json
[
  {
    "command_url": "http://localhost:8086/HTTPLoadTest-baseline/token?grant_type=client_credentials&client_id=#CLIENT_ID&client_secret=#CLIENT_SECRET",
    "command_contenttype": "application/json;charset=UTF-8",
    "command_http_authstring": "",
    "command_http_post": true,
    "command_timeout_milliseconds": 200,
    "command_template": "",
    "command_replacement_map": {
      "#CLIENT_ID":"myClientIdValue",
      "#CLIENT_SECRET":"MySecretValue"
    },
    "command_response_map": {
      "#access_token": "$..access_token"
    }
  }
  ,
  {
    "command_url" : "http://test.me/#MySite",
    "command_contenttype" : "application/json",
    "command_http_authstring": "Basic ZGV2ZWxvcG1lbnQtbWVkaWNhdGlvbjoxT09jTEpoR01oSyMxMHUhRDMzUw==",
    "command_http_post" : true,
    "command_timeout_milliseconds" : 5000,
    "command_template" : "{\n  \\\"sub\\\": \\\"#fizzle(digits:67643)\\\",\n  \\\"name\\\": \\\"#UserID\\\",\n  \\\"admin\\\": true\n}",
    "command_replacement_map" : {
      "#MySite" : "demo"
  },
    "command_response_map" : {
      "#randomizeName" : "$..test_randomize_sleeptime",
      "#testName" : "$..test_name"
    }
  } 
]
```
From the example above, we can see use of several of HTTPLoadTest features. The command_replacement_map is a supplied
variable-set for the given command. This is used to replace parts of an URL or parts of an HTTP payload. The
command_response_map consist of JsonPath expression which is matched against the response and put into the
corresponding named variable. 

The generated variables can then be used in consequitive commands along with the variables in the command_replacement_map. If you need  a list as a result, you might find the #fizzle(option:#variablename) feature handy. This will pick a random value
from the returned response and substitute it in the next command.  As of now, OAUTH2 and http basic-auth is supported. You
can use pre-calculated  and "username/password" or "username:password" is supported.

Note: Please note that the variables are shared between the read TestSpesification and the write TestSpecification to allow
sharing of data between the different simulated scenarios. As the variables is inherited in strict order (global, load-test
defined, command-defined and resolved from earlier commands). You might want to condider using a naming scheme for variables
to avoid values beeing overwritten and thus causing problems for your test-runs.


#### Template (command_template) and URL (command_url) special operations

```properties
#fizzle(chars:replaceMe)                          =>  tEftohTdS
#fizzle(digits:67643)                             =>  32632
#fizzle(U_chars:(TEST)                            =>  WRVY
#fizzle(L_chars:(lower)                           =>  tgewt
#fizzle(HEX:(a hexvalue)                          =>  4E7AD3B084
#fizzle(option:yes, no, here, there)              =>  here
```


Tip: A very useful tip when you are building and testing TestSpecifications is to visit the /health endpoint, as it will
display specifications with the variables resolved so you can verify that the replacement is what you wanted it to be.


### Example on write test specification
```json
[
  {
    "command_url": "http://localhost:8086/HTTPLoadTest-baseline/token?grant_type=client_credentials&client_id=#CLIENT_ID&client_secret=#CLIENT_SECRET",
    "command_contenttype": "application/json;charset=UTF-8",
    "command_http_authstring": "",
    "command_http_post": true,
    "command_timeout_milliseconds": 200,
    "command_template": "",
    "command_replacement_map": {
      "#CLIENT_ID": "myClientIdValue",
      "#CLIENT_SECRET": "MySecretValue"
    },
    "command_response_map": {
      "#access_token": "$..access_token"
    }
  },
  {
    "command_url": "http://localhost:8086/HTTPLoadTest-baseline/token?grant_type=authorization_code&code=#code&redirect_uri=#redirectURI&client_id=#CLIENT_ID&client_secret=#CLIENT_SECRET",
    "command_contenttype": "application/json;charset=UTF-8",
    "command_http_authstring": "",
    "command_http_post": true,
    "command_timeout_milliseconds": 200,
    "command_template": "",
    "command_replacement_map": {
      "#code": "myDummyCode",
      "#redirectURI": "https://www.vg.no"
    },
    "command_response_map": {
      "#access_token2": "$..access_token",
      "#token_type": "$..token_type",
      "#expires_in": "$..expires_in",
      "#refresh_token": "$..refresh_token",
      "#scope": "$..scope",
      "#uid": "$..uid",
      "#info": "$..info"
    }
  },
  {
    "command_url": "http://localhost:8086/HTTPLoadTest-baseline/verify",
    "command_contenttype": "application/json;charset=UTF-8",
    "command_http_authstring": "Bearer #fizzle(option:#access_token)",
    "command_http_post": false,
    "command_timeout_milliseconds": 200,
    "command_template": "",
    "command_replacement_map": {
    },
    "command_response_map": {
      "#client_id": "$..client_id",
      "#auth_user_id": "$..auth_user_id",
      "#user_id": "$..user_id",
      "#user_type": "$..user_type",
      "#expires": "$..expires",
      "#scope": "$..scope",
      "#name": "$..name"
    }
  }
]
```

In this example, we use the embedded Oauth2 server simulator to examplify an authorization flow as a part of a test with
chaining of variables down the chain to use the established oauth2 session in later calls. As of now, HTTPLoadTest-Baseline
support jsonpath and xpath for parsing results into variables.


#### If you want to provision several sets of TestSpecifications, you can add something like this to ./config_override/application_override.properties
```properties
TestSpecification.1.displayname=Bootstrap TestSpecifications
TestSpecification.1.read.filename=./specifications/FirstReadTestSpecification.json
TestSpecification.1.write.filename=./specifications/FirstWriteTestSpecification.json
TestSpecification.2.displayname=Normal Simulated TestSpecifications
TestSpecification.2.read.filename=./specifications/SecondReadTestSpecification.json
TestSpecification.2.write.filename=./specifications/SecondWriteTestSpecification.json
```

#### If you want completely global variables, you might add something like this to ./config_override/application_override.properties
```properties
GlobalVariable.1.name=#TestMe
GlobalVariable.1.value={per, ola, petter}
GlobalVariable.2.name=#SystemID
GlobalVariable.2.value=PDC-10
```

# Protecting the LoadTest WebUI

If you want to add basic authentication to HTTLLoadTest, just add the following to config_override/application_override.properties
```properties
loadtest.basicauth=true
#
login.admin.user=admin
login.admin.password=adminservice
#
# loadtest.breakflowonfailure=true
```

# Setting benchmarks for evaluating your load-test run(s)
```json
{
  "benchmark_id": "BenchmarkID",
  "benchmark_req_sucessrate_percent": 99,
  "benchmark_req_mean_read_duration_ms": 1010,
  "benchmark_req_mean_write_duration_ms": 1020,
  "benchmark_req_90percentile_read_duration_ms": 910,
  "benchmark_req_90percentile_write_duration_ms": 920
}
```

The HTTPLoadTest-baseline comes with a pre-configured benchmark for load-test runs, which is defined in
DefaultLoadTestBenchmark.json. 

* [Clustering HTTPLoadTest-baseline](CLUSTERING.md)


# Some ideas of advanced usage

Since HTTPLoadTest is an API-based load-test platform it can be fun to create test-runs, which create new TestSpecifications
and then run and verify the generated tests. I.e. you can use this to "discover" URI/Paths of an application, and du simple
variations to discover potentionally security issues, DoS vectors and the like. If time permits, we'll see if we can make a
short intro/demo of such usage sometime in the future.


# Development and Contribution

HTTPLoadTest-baseline is still in its early days, but has already proven to be very useful in many sitiuations. We encourage
you to take it for a spin. If you fork the codebase and use it as a starting poing for making your specialized version or
want to contribute to the baseline codebase is up to you. We love feedback, so please tell us what you think and do not be
afraid to use the github issues to flag bugs or new feature requests. We also love comitters and pull-request, so don't be
shy...
 

### Quick build and verify

```jshelllanguage
mvn clean install
#
java -jar target/HTTPLoadTest-baseline-<version>-SNAPSHOT.jar
#
wget http://localhost:8086/HTTPLoadTest-baseline/health
wget http://localhost:8086/HTTPLoadTest-baseline/config                      // UI to configure a loadTest Run
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/status             // return latest loadTests with status
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/stop               // stop all loadTests
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus         // return alle the loadTest results in json
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus_csv     // return alle the loadTest results in csv
wget -o result.txt http://localhost:28086/HTTPLoadTest-baseline/loadTest/runstatus  // runstatus against loadtestbenchmark status
#
## To download results from earlier test-runs, use /health to find the name of the test-run, and 
#
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus_csv?test_id=HTTPLOadTest-Health_1516003617097.csv
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus?test_id=HTTPLOadTest-Health_1516003617097.json
```
* version=0.37.2 

Open in browser:  
* To configure and start a load test: http://localhost:8086/HTTPLoadTest-baseline/config   
* To configure the read-testdriver: http://localhost:8086/HTTPLoadTest-baseline/config/read   
* To configure the write-testdriver: http://localhost:8086/HTTPLoadTest-baseline/config/write   
* To select between pre-configured TestSpecifications sets: http://localhost:8086/HTTPLoadTest-baseline/config/select


### Work-in-progress - nice vizualisation of running loadtests

Since the main use meant for HTTPLoadTest is in automated QA pipelines, we have initially not spent much effort in 
nice WebUI and result vizualizations. We hope to address this shortcoming some time in the future and will be glad 
for any helping hands.

![Ugly UI whiteboard mockup](https://raw.githubusercontent.com/Cantara/HTTPLoadTest-Baseline/master/images/whiteboard-UI-config-mockup.jpg)


### Troubleshoting   

*Maven fails*
```jshelllanguage
> mvn -version
Requires Java 8
> export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
> mvn clean install
```
