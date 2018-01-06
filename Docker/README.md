# HTTPLoadTest-baseline

##### Status

![Build Status](https://jenkins.capraconsulting.no/buildStatus/icon?job=Cantara-HTTPLoadTest-baseline) - [![Project Status: Active – The project has reached a stable, usable state and is being actively developed.](http://www.repostatus.org/badges/latest/active.svg)](http://www.repostatus.org/#active) 

[![Known Vulnerabilities](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline/badge.svg)](https://snyk.io/test/github/Cantara/HTTPLoadTest-baseline)


A simple baseline for building LoadTests to be used for continous deploy/continous production QA pipelines.  Baseline projects are meant to be a git clone starting point for for software which are expected to grow and flourish in different ways which are not easy to parameterize in early stages. It should be useable for quite a few settings, but is expected to grow in different directions. We would love to receive pull-request for enhancements both on current codebase and extensibillity features.

Why another load-test OpenSource project?  We think this is a reasonable question. There exist quite a few "full-fledged" alternatives. Our concern is that the uptake of those solutions are way below what the industry need, and this is an attempt to try to offer a different alternative which may or may not fulfill you needs/requirements. 

Coming from development backgrounds, we hope that a baseline you might contribute to, or just form and change to your requirements/needs might increase the quality of produces software by making it less "expensive" to add this type of quality processes into your software development process.

#### The process-flow of load-testing

![The flow of LoadTest investments](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/HTTPLoadTest-FullProcessFlow.png)

#### A quick intro/test-run - Check it in docker

```
sudo docker run -d -p 28086:8086  cantara/httploadtest-baseline
wget http://localhost:28086/HTTPLoadTest-baseline/health
```
Open in browser:  
* To configure and start a load test: http://localhost:28086/HTTPLoadTest-baseline/config   


##n More documentation
* https://github.com/Cantara/HTTPLoadTest-Baseline/blob/master/README.md



# Building the Docker container 

## How it works
* The configuration override of the application is volume mounted when running the Docker image

## Prerequisites
* Docker daemon running (see https://wiki.cantara.no/display/FPP/Docker+cheat+sheet)

## Set up

### Initial install

#### Building config
Create a configuration file, e.g. `config_override.properties`.

#### Creating Docker-instance
* Make sure the config file exists
* Skip `--restart=always` if doing this locally to avoid it to start with your computer.

Connecting to instance for debugging:
```bash
docker exec -it -u HTTPLoadTest-baseline HTTPLoadTest-baseline bash
```

## Testing docker-build locally
See [test-docker.sh](test-docker.sh).

This script can be run with `./test-docker.sh local` to also run `mvn package` and use jar from development.

#### Quickly verify the image
```bash
wget http://localhost:18086/HTTPLoadTest-baseline/health
```

Open in browser:  
* http://localhost:18086/HTTPLoadTest-baseline/config


Ypu may also use the /scripts/run_from_dockerhub.sh script to run the latest pre-built dockerimage directly form dockerhub