#!/bin/bash

wget -v -d "jsonConfig=@ReadTestSpecification-TestHealth.json" -H "Content-Type: application/x-www-form-urlencoded" --user admin --password admin  -post-data http://localhost:8086/HTTPLoadTest-baseline/loadTest/form/read
wget -v -d "jsonConfig=@WriteTestSpecification_TestOauth2ProtectedResource.json" -H "Content-Type: application/x-www-form-urlencoded" --user admin --password admin   -post-datahttp://localhost:8086/HTTPLoadTest-baseline/loadTest/form/write
wget -v -d "jsonConfig=@@LoadTestConfig.json" -H "Content-Type: application/x-www-form-urlencoded" --user admin --password admin  -post-data http://localhost:8086/HTTPLoadTest-baseline/loadTest/form

##  wait until test is complete
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:8086/HTTPLoadTest-baseline/loadTest/runstatus)" == "409" ]]; do sleep 5; done
# get the result
#if [ curl -s -o /dev/null -w ''%{http_code}'' localhost:8086/HTTPLoadTest-baseline/loadTest/runstatus)" == "200"  ]; then
#   echo "LoadTest run was marked success for benchmark criterias"
#fi
wget -o result.txt http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus
## check the results.txt against QA rules
wget -o benchmark.txt http://localhost:8086/HTTPLoadTest-baseline/loadTest/runstatus
## To download results from earlier test-runs, use /health to find the name of the test-run, and
#wget  -o mycsv.csv http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus_csv?test_id=HTTPLOadTest-Health_1516003617097.csv
echo "Complete"
cat benchmark.txt