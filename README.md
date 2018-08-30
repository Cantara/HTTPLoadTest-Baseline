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



#### A quick intro - getting started


* [Tutorial on how to use HTTPLoadTest-baseline](doc/tutorial/index.md)
* [Example walk-through of HTTPLoadTest](./doc/httploadtest-example-run.md)

```jshelllanguage
sudo docker run -d -p 28086:8086  cantara/httploadtest-baseline
wget http://localhost:28086/HTTPLoadTest-baseline/health
```
Then open the simple UI in a web browser:  
* [To configure and start a load test](http://localhost:28086/HTTPLoadTest-baseline/config)   


### Quick overview of the key data concepts
![The LoadTest data structures](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/images/HTTPLoadTest-DataStructures.png)

### A typical example of the result of a Benchmark Result
http://localhost:28086/HTTPLoadTest-baseline/loadTest/runstatus
```json
{
  "benchmark_req_90percentile_read_duration_ms" : "true",
  "benchmark_req_90percentile_write_duration_ms" : "true",
  "benchmark_req_95percentile_read_duration_ms" : "true",
  "benchmark_req_95percentile_write_duration_ms" : "true",
  "benchmark_req_99percentile_read_duration_ms" : "true",
  "benchmark_req_99percentile_write_duration_ms" : "false",
  "benchmark_req_mean_read_duration_ms" : "true",
  "benchmark_req_mean_write_duration_ms" : "true",
  "benchmark_req_sucessrate_percent" : "true",
  "isBenchmarkPassed" : "false",
  "runstatus" : "fail",
  "stats_o_deviations" : "0",
  "stats_o_failures" : "0",
  "stats_o_results" : "0",
  "stats_o_success" : "0",
  "stats_r_deviations" : "0",
  "stats_r_duration_ms" : "228282",
  "stats_r_failures" : "0",
  "stats_r_mean_success_ms" : "108",
  "stats_r_median_success_ms" : "80",
  "stats_r_ninety_percentine_success_ms" : "204",
  "stats_r_ninetyfive_percentine_success_ms" : "277",
  "stats_r_ninetynine_percentine_success_ms" : "748",
  "stats_r_results" : "2110",
  "stats_r_success" : "2110",
  "stats_t_deviations" : "0",
  "stats_t_failures" : "0",
  "stats_t_results" : "2300",
  "stats_t_success" : "2300",
  "stats_total_successrate" : "100",
  "stats_w_deviations" : "0",
  "stats_w_duration_ms" : "52139",
  "stats_w_failures" : "0",
  "stats_w_mean_success_ms" : "274",
  "stats_w_median_success_ms" : "221",
  "stats_w_ninety_percentine_success_ms" : "446",
  "stats_w_ninetyfive_percentine_success_ms" : "893",
  "stats_w_ninetynine_percentine_success_ms" : "1307",
  "stats_w_results" : "190",
  "stats_w_success" : "190",
  "timestamp" : "1516966252860"
}
```

### Example-oriented documentation to get familiar with the application

Since the main goal for this codebase is to simplify Companies efforts in ensuring that an agile or continous investment into
software development does not compromise the quality assurance processes on non-functional requirements, we have tried to
focus on making the load-test QA process easily embeddable to a Company's continous CI/CD processes. This means that the
application have two modi:  
* a) an exploration modi - where you (as in any normal QA process, can analyze the load-characteristics of your application
(load-limits, behaviour with excessive load, endurance charasteristics under high load and the like) 
* b) easily add the baseline characteristics as benchmarks to the deployment QA pipelines and detect when changes break the
expectations.

* [Example walk-through of HTTPLoadTest](./doc/httploadtest-example-run.md)




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
#
## Upload a zip-file with specifications
curl -F "file=@specifications.zip;filename=specifications.zip" http://localhost:8086/HTTPLoadTest-baseline/loadTest/zip
```
* version=0.47.2 

Open in browser:  
* To configure and start a load test: http://localhost:8086/HTTPLoadTest-baseline/config   
* To configure the read-testdriver: http://localhost:8086/HTTPLoadTest-baseline/config/read   
* To configure the write-testdriver: http://localhost:8086/HTTPLoadTest-baseline/config/write   
* To select between pre-configured TestSpecifications sets: http://localhost:8086/HTTPLoadTest-baseline/config/select


### Work-in-progress - nice vizualisation of running loadtests

Since the main use meant for HTTPLoadTest is in automated QA pipelines, we have initially not spent much effort in 
nice WebUI and result visualization. We hope to address this shortcoming some time in the future and will be glad 
for any helping hands.

![Example Visualization](https://raw.githubusercontent.com/Cantara/HTTPLoadTest-Baseline/master/doc/HTTPLoadTest-Output-Visualization.png)


![Ugly UI whiteboard mockup](https://raw.githubusercontent.com/Cantara/HTTPLoadTest-Baseline/master/images/whiteboard-UI-config-mockup.jpg)


### Troubleshoting   

*Maven fails*
```jshelllanguage
> mvn -version
Requires Java 8
> export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
> mvn clean install
```
