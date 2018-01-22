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