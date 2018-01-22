# HTTPLoadTest-Baseline

##### Status
![Build Status](https://jenkins.capraconsulting.no/buildStatus/icon?job=Cantara-HTTPLoadTest-baseline) - 
[![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](http://www.repostatus.org/badges/latest/active.svg)](http://www.repostatus.org/#active)    [![Known Vulnerabilities](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline/badge.svg)](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline)


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


