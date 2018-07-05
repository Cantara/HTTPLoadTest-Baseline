# Running LoadTests from CI servers

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


* With your automation completed, it's time to [explore advanced topics and use of HTTPLoadTest](./0-advanced-topics.md).