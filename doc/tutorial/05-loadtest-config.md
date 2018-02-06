# Specifying the Load


### Example LoadTestConfig
```json
{
  "test_id": "MyTestID",
  "test_name": "An example of load test configuration",
  "test_no_of_threads": 10,
  "test_read_write_ratio": 90,
  "test_sleep_in_ms": 10,
  "test_randomize_sleeptime": true,
  "test_duration_in_seconds": 20,
  "test_global_variables_map": {
    "#Password": "MyTestPasseord",
    "#UserID": "MyTestUser"
  }
}
```

The LoadTestConfig is the configuration of the top-level parameters of a load test and control the behaviour of the two
(read/write) TestSpesifications (see further down for more information and examples of how to configure TestSpecifications).
The LoadTestConfig also consist of the load-time, which is the longest allowed time the tests are allowed to run.  You can
define LoadTest global variables for each LoadTestConfig.
