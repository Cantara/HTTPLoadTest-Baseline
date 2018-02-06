# Read vs Write flows

### Quick overview of the key data concepts
![The LoadTest data structures](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/images/HTTPLoadTest-DataStructures.png)


## Simulating "read" flow

A rule of thumb when it comes to define the steps in a read flow include the following questions
 - Is the data protected by authentication?  If so, create the "log-on" steps (I.e.  basic authentication, OAUTH2 or similar)
    Note: A simulated "read" might include HTTP Post operations, this is "normal"
 - What data is typically fetched when a user access the module or application. Create the steps to receive this information. 
    Note: If it likely that the steps above will be agressively cached by the application, add a step to the "write" flow to update and thus invalidate
    the cache.


* With your simulation flow complete, it's time to [play with how you want to apply load to the system(s)](./05-loadtest-config.md).