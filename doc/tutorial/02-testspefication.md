# Specifying HTTP requests

![The LoadTest data structures](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/images/HTTPLoadTest-DataStructures-Detailed.png)

### Example on a test specification
```json
[
  {
    "command_url": "http://localhost:8086/HTTPLoadTest-baseline/token?grant_type=client_credentials&client_id=#CLIENT_ID&client_secret=#CLIENT_SECRET",
    "command_contenttype": "application/json;charset=UTF-8",
    "command_http_authstring": "",
    "command_http_post": true,
    "command_timeout_milliseconds": 200,
    "command_template": "",
    "command_replacement_map": {
      "#CLIENT_ID": "myClientIdValue",
      "#CLIENT_SECRET": "MySecretValue"
    },
    "command_response_map": {
      "#access_token": "$..access_token"
    }
  }
]
```




#### Template (command_template) and URL (command_url) special operations

```properties
#fizzle(chars:replaceMe)                             =>  tEftohTdS
#fizzle(digits:67643)                                =>  32632
#fizzle(U_chars:TEST)                                =>  WRVY
#fizzle(L_chars:lower)                               =>  tgewt
#fizzle(HEX:a hexvalue)                              =>  4E7AD3B084
#fizzle(option:yes, no, here, there)                 =>  here
#fizzle(optionvalue:{"yes", "no", "here", "there"})  =>  here
#fizzle(substring(4,7):one two three)                =>  two
#fizzle(timestamp:yyyy-MM-dd HH:mm:ss)               =>  2018-08-20 11:39:05
```

* With your fist HTTPRequest complete, it's time to [see how to chain results form one request into the next request](./03-chaining-requests.md).