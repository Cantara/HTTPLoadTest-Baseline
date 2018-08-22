# Chaining HTTP request flows


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
  },
  {
    "command_url": "http://localhost:8086/HTTPLoadTest-baseline/token?grant_type=authorization_code&code=#code&redirect_uri=#redirectURI&client_id=#CLIENT_ID&client_secret=#CLIENT_SECRET",
    "command_contenttype": "application/json;charset=UTF-8",
    "command_http_authstring": "",
    "command_http_post": true,
    "command_timeout_milliseconds": 200,
    "command_template": "",
    "command_replacement_map": {
      "#code": "myDummyCode",
      "#redirectURI": "https://www.vg.no"
    },
    "command_response_map": {
      "#access_token2": "$..access_token",
      "#token_type": "$..token_type",
      "#expires_in": "$..expires_in",
      "#refresh_token": "$..refresh_token",
      "#scope": "$..scope",
      "#uid": "$..uid",
      "#info": "$..info"
    }
  },
  {
    "command_url": "http://localhost:8086/HTTPLoadTest-baseline/verify",
    "command_contenttype": "application/json;charset=UTF-8",
    "command_http_authstring": "Bearer #fizzle(option:#access_token)",
    "command_http_post": false,
    "command_timeout_milliseconds": 200,
    "command_template": "",
    "command_replacement_map": {
    },
    "command_response_map": {
      "#client_id": "$..client_id",
      "#auth_user_id": "$..auth_user_id",
      "#user_id": "$..user_id",
      "#user_type": "$..user_type",
      "#expires": "$..expires",
      "#scope": "$..scope",
      "#name": "$..name"
    }
  }
]
```

In this example, we use the embedded Oauth2 server simulator to examplify an authorization flow as a part of a test with
chaining of variables down the chain to use the established oauth2 session in later calls. As of now, HTTPLoadTest-Baseline
support jsonpath and xpath for parsing results into variables.

#### Template (command_template) and URL (command_url) special operations

```properties
#fizzle(chars:replaceMe)                             =>  tEftohTdS
#fizzle(digits:67643)                                =>  32632
#fizzle(U_chars:(TEST)                               =>  WRVY
#fizzle(L_chars:(lower)                              =>  tgewt
#fizzle(HEX:(a hexvalue)                             =>  4E7AD3B084
#fizzle(option:yes, no, here, there)                 =>  here
#fizzle(optionvalue:{"yes", "no", "here", "there"})  =>  here
#fizzle(substring(4,7):one two three)                =>  two
#fizzle(timestamp:yyyy-MM-dd HH:mm:ss)               =>  2018-08-20 11:39:05
```

* With your fist simulated flow complete, it's time to [learn how read and write flow differ](./04-read-write-flows.md).