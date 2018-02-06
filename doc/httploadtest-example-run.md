### HTTPLoadTest, used as pipeline example (bash example)

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

    7661 read tests resulted in 7606 successful runs where 55 was marked failure and 0 was marked as deviation(s).
     783 write tests resulted in 783 successful runs where 0 was marked failure and 0 was marked as deviation(s).
       0 unmarked tests resulted in 0 successful runs where 0 was marked failure and  0 was marked as deviation(s).
    8444 total tests resulted in 8389 successful runs where 55 was marked failure and 0 was marked as deviation(s).
    8504 tasks scheduled, number of threads configured: 30, isRunning: false 
     305 ms mean duration,  379 ms 90% percentile,  411 ms 95% percentile,  577 ms 99% percentile successful read tests
     561 ms mean duration,  664 ms 90% percentile,  701 ms 95% percentile,  838 ms 99% percentile successful write tests

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

![The LoadTest data structures](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/images/HTTPLoadTest-DataStructures-Detailed.png)


#### Template (command_template) and URL (command_url) special operations

```properties
#fizzle(chars:replaceMe)                             =>  tEftohTdS
#fizzle(digits:67643)                                =>  32632
#fizzle(U_chars:(TEST)                               =>  WRVY
#fizzle(L_chars:(lower)                              =>  tgewt
#fizzle(HEX:(a hexvalue)                             =>  4E7AD3B084
#fizzle(option:yes, no, here, there)                 =>  here
#fizzle(optionvalue:{"yes", "no", "here", "there"})  =>  here
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

# Or just let HTTPLoadTest locate read/write TestSpecifications on the filesystem
loadtest.testspecification.rootpath=./specifications
loadtest.testspecification.read.filematcher=*ReadTest*.json
loadtest.testspecification.write.filematcher=*WriteTest*.json

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
  "benchmark_id" : "BenchmarkID",
  "benchmark_req_sucessrate_percent" : 99,
  "benchmark_req_mean_read_duration_ms" : 1010,
  "benchmark_req_mean_write_duration_ms" : 1020,
  "benchmark_req_90percentile_read_duration_ms" : 910,
  "benchmark_req_90percentile_write_duration_ms" : 920,
  "benchmark_req_95percentile_read_duration_ms" : 1110,
  "benchmark_req_95percentile_write_duration_ms" : 1120,
  "benchmark_req_99percentile_read_duration_ms" : 1130,
  "benchmark_req_99percentile_write_duration_ms" : 1140
}
```

The HTTPLoadTest-baseline comes with a pre-configured benchmark for load-test runs, which is defined in
DefaultLoadTestBenchmark.json. Only "benchmark_id" and "benchmark_req_sucessrate_percent" are required values. If you specify
"0" or omit the json property no validation will be done for that requirement.

* [Clustering HTTPLoadTest-baseline](doc/CLUSTERING.md)


# Some ideas of advanced usage

Since HTTPLoadTest is an API-based load-test platform it can be fun to create test-runs, which create new TestSpecifications
and then run and verify the generated tests. I.e. you can use this to "discover" URI/Paths of an application, and du simple
variations to discover potentionally security issues, DoS vectors and the like. If time permits, we'll see if we can make a
short intro/demo of such usage sometime in the future.

