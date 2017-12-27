# HTTPLoadTest-baseline

A typical simple baseline for building good microservices


![Build Status](https://jenkins.capraconsulting.no/buildStatus/icon?job=Cantara-HTTPLoadTest-baseline) - [![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](http://www.repostatus.org/badges/latest/active.svg)](http://www.repostatus.org/#active) 

[![Known Vulnerabilities](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline/badge.svg)](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline)

Documentation
* https://wiki.cantara.no/display/architecture/Typical+micro+service+technology+stack+(java)


Quick build and verify
'''
* mvn clean install
* java -jar target/microservice-baseline-0.1-SNAPSHOT.jar
* wget http://localhost:8086/HTTPLoadTest-baseline/health
* wget "http://localhost:8086/microservice-baseline/setup
'''
