# Tutorial

This tutorial will walk you through writing a HTTPLoadTest from scratch. 

HTTPLoadTest-Baseline is simple starting point for building LoadTests to be used for continuous deploy/continuous production
QA pipelines. Baseline projects are meant to be a git clone starting point for for software which are expected to grow and
flourish in different ways which are not easy to parameterize in early stages. It should be usable for quite a few settings,
but is expected to grow in different directions. We would love to receive pull-request for enhancements both on current
codebase and extensibility features.


1. [Planning a loadtest](01-planning.md)
2. [Specifying HTTP requests](02-testspefication.md)
3. [Chaining HTTP request flows](03-chaining-requests.md)
4. [Read vs Write flows](04-read-write-flows.md)
5. [Specifying the Load](05-loadtest-config.md)
6. [Running and establishing LoadTest benchmarks](06-benchmarks.md)
7. [Running LoadTests from CI servers](07-running-from-CI.md)
8. [Embedding loadtests](08-running-as-embedded-tests.md)
9. [Advanced Topics](09-advanced-topics.md)

![The flow of LoadTest investments](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/images/HTTPLoadTest-FullProcessFlow.png)
