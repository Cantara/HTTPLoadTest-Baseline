# HTTPLoadTest Frequently Asked Questions

## General


#### Why do I get smaller load per thread on HTTPLoadTest than on my own code.
Usually this is a result of a) using/copying  "test_sleep_in_ms": 10 in TestSpecifications and/or b) misinterpretation of how HTTPLOadTest TestSpecifications
differ from similar products by forgetting that a TestSpecification is a linked list/chain of HTTP-requests, and the default measurement is for the complete HTTP-reequest flow.

#### Why HTTPLoadTest report much more latency on requests than my own code?
Usually this is a result of a misinterpretation of how HTTPLOadTest TestSpecifications differ from similar products. In HTTPLoadTest a TestSpecification
is a linked list/chain of HTTP-requests, and the default measurement is for the complete HTTP-reequest flow.
