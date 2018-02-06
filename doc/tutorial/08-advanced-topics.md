# Advanced Topics


### HTTP 302

Since many authorization flows make use of HTTP 302 Location HTTP Header redirects, the HTTPLoadTest requests will detect such redirect requests and 
create a URL-parameter map which is returned to the execution flow. For oauth2 authentication you can then easily assign the OAUTH2 code to a variable
which you can use to get the OAUTH2 token.

### OAUTH2

Since OAUTH2 is more or less the common protocol to protect services, here is a quick example of how your OAUTH2 authentication might look like. 

```json
[
  {
    "command_url": "#OAUTH2Server/authorize?client_id=#CLIENT_ID&client_secret=#CLIENT_SECRET&response_type=code&redirect_uri=#REDIRECT_URI/authorize&state=placeholder&user_id=00000000-0000-4000-a007-000000000001",
    "command_contenttype": "application/json;charset=UTF-8",
    "command_http_authstring": "X-AUTH-PROTOCOL-VERSION: 2",
    "command_http_post": false,
    "command_timeout_milliseconds": 300,
    "command_template": "",
    "command_replacement_map": {
      "#OAUTH2Server": "https://my.oauth2.server.com/auth",
      "#REDIRECT_URI": "https://my-server.com/myapp/api",
      "#CLIENT_ID": "6487234-0000-4000-a006-0342142009",
      "#CLIENT_SECRET": "uqewioruiqower8"
    },
    "command_response_map": {
      "#code": "$..code"
    }
  },
  {
    "command_url": "#OAUTH2Server/token",
    "command_contenttype": "application/x-www-form-urlencoded;charset=UTF-8",
    "command_http_authstring": "#CLIENT_ID:#CLIENT_SECRET",
    "command_http_post": true,
    "command_timeout_milliseconds": 300,
    "command_template": "grant_type=authorization_code&code=#fizzle(optionvalue:#code)&redirect_uri=#REDIRECT_URI/authorize",
    "command_replacement_map": {
    },
    "command_response_map": {
      "#access_token": "$..access_token",
      "#token_type": "$..token_type",
      "#expires_in": "$..expires_in",
      "#refresh_token": "$..refresh_token",
      "#refresh_expires_in": "$..refresh_expires_in",
      "#scope": "$..scope"
    }
  }
]
```



### Hazelcast setup

* [Clustering HTTPLoadTest-baseline](CLUSTERING.md)


##### Some ideas of advanced usage

Since HTTPLoadTest is an API-based load-test platform it can be fun to create test-runs, which create new TestSpecifications
and then run and verify the generated tests. I.e. you can use this to "discover" URI/Paths of an application, and du simple
variations to discover potentionally security issues, DoS vectors and the like. If time permits, we'll see if we can make a
short intro/demo of such usage sometime in the future.


### Extending the HTTPLoadTest docker-image  

```jshelllanguage
FROM cantara/httploadtest-baseline:latest

add config_override /home/HTTPLoadTest-baseline/config_override
add specifications /home/HTTPLoadTest-baseline/specifications
```



#### If you want to provision several sets of TestSpecifications, you can add something like this to ./config_override/application_override.properties
```properties
TestSpecification.1.displayname=Bootstrap TestSpecifications
TestSpecification.1.read.filename=./specifications/FirstReadTestSpecification.json
TestSpecification.1.write.filename=./specifications/FirstWriteTestSpecification.json
TestSpecification.2.displayname=Normal Simulated TestSpecifications
TestSpecification.2.read.filename=./specifications/SecondReadTestSpecification.json
TestSpecification.2.write.filename=./specifications/SecondWriteTestSpecification.json

# Or just let HTTPLoadTest locate read/write TestSpecifications on the filesystem
loadtest.testspecification.rootpath=./specifications
loadtest.testspecification.read.filematcher=*ReadTest*.json
loadtest.testspecification.write.filematcher=*WriteTest*.json

```

#### If you want completely global variables, you might add something like this to ./config_override/application_override.properties
```properties
GlobalVariable.1.name=#TestMe
GlobalVariable.1.value={per, ola, petter}
GlobalVariable.2.name=#SystemID
GlobalVariable.2.value=PDC-10
```

#### Protecting the LoadTest WebUI

If you want to add basic authentication to HTTLLoadTest, just add the following to config_override/application_override.properties
```properties
loadtest.basicauth=true
#
login.admin.user=admin
login.admin.password=adminservice
#
# loadtest.breakflowonfailure=true
```


### API documentation

```jshelllanguage
#
wget http://localhost:8086/HTTPLoadTest-baseline/health
wget http://localhost:8086/HTTPLoadTest-baseline/config                      // UI to configure a loadTest Run
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/status             // return latest loadTests with status
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/stop               // stop all loadTests
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus         // return alle the loadTest results in json
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus_csv     // return alle the loadTest results in csv
wget -o result.txt http://localhost:28086/HTTPLoadTest-baseline/loadTest/runstatus  // runstatus against loadtestbenchmark status
#
## To download results from earlier test-runs, use /health to find the name of the test-run, and 
#
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus_csv?test_id=HTTPLOadTest-Health_1516003617097.csv
wget http://localhost:8086/HTTPLoadTest-baseline/loadTest/fullstatus?test_id=HTTPLOadTest-Health_1516003617097.json
#
## Upload a zip-file with specifications
curl -F "file=@specifications.zip;filename=specifications.zip" http://localhost:8086/HTTPLoadTest-baseline/loadTest/zip
```