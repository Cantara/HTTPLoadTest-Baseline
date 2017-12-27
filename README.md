# HTTPLoadTest-baseline

A typical simple baseline for building LoadTest for continous deploy/continous production QA pipelines


![Build Status](https://jenkins.capraconsulting.no/buildStatus/icon?job=Cantara-HTTPLoadTest-baseline) - [![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](http://www.repostatus.org/badges/latest/active.svg)](http://www.repostatus.org/#active) 

[![Known Vulnerabilities](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline/badge.svg)](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline)


![Ugly UI whiteboard mockup](https://raw.githubusercontent.com/Cantara/HTTPLoadTest-Baseline/master/whiteboard-UI-config-mockup.jpg)

Documentation
* https://wiki.cantara.no/


Quick build and verify
'''
* mvn clean install
* java -jar target/HTTPLoadTest-baseline-0.1-SNAPSHOT.jar
* wget http://localhost:8086/HTTPLoadTest-baseline/health
* wget http://localhost:8086/HTTPLoadTest-baseline/config                      // UI to configure a loadTest Run
* wget http://localhost:8086/HTTPLoadTest-baseline/loadTest                    // return all loadTests with statuses
* wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/loadtestId/status  // return status for loadtest with loadTestid
'''

Open in browser:  
* http://localhost:8086/HTTPLoadTest-baseline/config
