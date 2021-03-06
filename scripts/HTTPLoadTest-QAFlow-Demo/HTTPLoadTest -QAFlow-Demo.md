# HTTPLoadTest-Baseline


HTTPLoadTest-Baseline is simple starting point for building LoadTests to be used for continuous deploy/continuous production QA pipelines. Baseline projects
are meant to be a git clone starting point for for software which are expected to grow and flourish in different ways which are not easy to parameterize in 
early stages. It should be usable for quite a few settings, but is expected to grow in different directions. We would love to receive pull-request for enhancements 
both on current codebase and extensibility features.

The main goal for this codebase is to simplify Companies efforts in ensuring that an agile or continuous investment into
software development does not compromise the quality assurance processes on non-functional requirements, we have tried to focus on making the load-test 
QA process easily embedable to a Company's continuous CI/CD processes.

Coming from development backgrounds, we hope that a baseline you might contribute to, or just form and change to your requirements/needs might increase 
the quality of produces software by making it less "expensive" to add this type of quality processes into your software development process.

#### The process-flow of load-testing

![The flow of LoadTest investments](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/images/HTTPLoadTest-FullProcessFlow.png)



### Pipeline usage example

#### The process is as follows
* Register ReadTestSpecification with HTTPLoadTest
* Register WriteTestSpecification with HTTPLoadTest
* Register LoadTestBenchmark with HTTPLoadTest
* Post LoadTestConfig and run the load test
* Loop while waiting for the Loadtest to complete
* Get the benchmark result of the load test run 
* Display the benchmark result


- The shellscript ./configure_and_run_loadtest.sh looks like this
```jshelllanguage
#!/bin/bash

JSON=$(<ReadTestSpecification-TestHealth.json ); wget -v --post-data "jsonConfig=${JSON}" -X POST  http://localhost:8086/HTTPLoadTest-baseline/loadTest/form/read
JSON=$(<WriteTestSpecification_TestOauth2ProtectedResource.json ); wget -v --post-data "jsonConfig=${JSON}" -X POST  http://localhost:8086/HTTPLoadTest-baseline/loadTest/form/write
JSON=$(<LoadTestBenchmark.json ); wget -v --post-data "jsonConfig=${JSON}" -X POST  http://localhost:8086/HTTPLoadTest-baseline/loadTest/form/benchmark
JSON=$(<LoadTestConfig.json ); wget -v --post-data "jsonConfig=${JSON}" -X POST  http://localhost:8086/HTTPLoadTest-baseline/loadTest/form


##  wait until test is complete
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:8086/HTTPLoadTest-baseline/loadTest/runstatus)" == "409" ]]; do sleep 5; done
# get the result
# if [ curl -s -o /dev/null -w ''%{http_code}'' localhost:8086/HTTPLoadTest-baseline/loadTest/runstatus)" == "200"  ]; then
#   echo "LoadTest run was marked success for benchmark criterias"
# fi
wget  http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus
## check the results.txt against QA rules
wget  --content-on-error http://localhost:8086/HTTPLoadTest-baseline/loadTest/runstatus
## To download results from earlier test-runs, use /health to find the name of the test-run, and
#  wget  -o mycsv.csv http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus_csv?test_id=HTTPLOadTest-Health_1516003617097.csv
echo "Complete"
cat runstatus
```


### An example on the returned json report below
```json
{
  "benchmark_req_90percentile_read_duration_ms" : "true",
  "benchmark_req_90percentile_write_duration_ms" : "true",
  "benchmark_req_mean_read_duration_ms" : "true",
  "benchmark_req_mean_write_duration_ms" : "true",
  "benchmark_req_sucessrate_percent" : "false",
  "isBenchmarkPassed" : "false",
  "runstatus" : "fail",
  "stats_o_deviations" : "0",
  "stats_o_failures" : "0",
  "stats_o_results" : "0",
  "stats_o_success" : "0",
  "stats_r_deviations" : "0",
  "stats_r_duration_ms" : "225270",
  "stats_r_failures" : "0",
  "stats_r_mean_success_ms" : "125",
  "stats_r_median_success_ms" : "85",
  "stats_r_ninety_percentine_success_ms" : "269",
  "stats_r_ninetyfive_percentine_success_ms" : "373",
  "stats_r_results" : "1797",
  "stats_r_success" : "1797",
  "stats_t_deviations" : "0",
  "stats_t_failures" : "1",
  "stats_t_results" : "1973",
  "stats_t_success" : "1972",
  "stats_total_successrate" : "0",
  "stats_w_deviations" : "0",
  "stats_w_duration_ms" : "56952",
  "stats_w_failures" : "1",
  "stats_w_mean_success_ms" : "325",
  "stats_w_median_success_ms" : "242",
  "stats_w_ninety_percentine_success_ms" : "662",
  "stats_w_ninetyfive_percentine_success_ms" : "1075",
  "stats_w_results" : "176",
  "stats_w_success" : "175",
  "timestamp" : "1516958088061"
}
```
