# Advanced Topics


### OAUTH2

### HTTP 302

### Hazelcast setup

* [Clustering HTTPLoadTest-baseline](CLUSTERING.md)


##### Some ideas of advanced usage

Since HTTPLoadTest is an API-based load-test platform it can be fun to create test-runs, which create new TestSpecifications
and then run and verify the generated tests. I.e. you can use this to "discover" URI/Paths of an application, and du simple
variations to discover potentionally security issues, DoS vectors and the like. If time permits, we'll see if we can make a
short intro/demo of such usage sometime in the future.


### Extrending the HTTPLoadTest docker-image  


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

# Protecting the LoadTest WebUI

If you want to add basic authentication to HTTLLoadTest, just add the following to config_override/application_override.properties
```properties
loadtest.basicauth=true
#
login.admin.user=admin
login.admin.password=adminservice
#
# loadtest.breakflowonfailure=true
```
