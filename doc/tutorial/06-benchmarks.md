# Running and establishing LoadTest benchmarks

### A typical example of the result of a Benchmark Result
http://localhost:2086/HTTPLoadTest-baseline/loadTest/runstatus
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


### Example on load test result
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


## Setting benchmarks for evaluating your load-test run(s)
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


* With your benchmarks complete, it's time to [look at how to use integrate HTTPLOadTest automation into your Continous Integration QA Flows](./07-running-from-CI.md).