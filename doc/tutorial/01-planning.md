# Planning a loadtest

HTTPLoadTest was built to try to make load testing both easy and realistic. They "key" concept is that you define two "typical" access-flows: "read" and "write".
The "read" request flow simulate the typical data-access for an application or module. I.e. what data is typically accessed by users/other applications. The "write" 
flow is similary an simulation of users or applications updating data of the module or application beeing tested. With these two flows designed, you the can set the 
ratio between read and write flows. (In many applications this is usually around 90% read).  With this in place, time comes to try out some load-levels. The number 
of threads define how many "workers" you want to have running your read and write flows.  The last value for the load-test is the number of seconds you want the test 
to last.  A typical number for normal load-tests is 100s, but if you wish, you may use 10400s for a more endurance test.  Another use-case is to let HTTPLoadTest 
generate a 50-70% "baseload" on your system while users, developers and other access it to pinpoint certain features which perform badly when the system is under 
high load.

So let uss summeriza by looking at the process-flow in the figure below. 

#### The process-flow of load-testing

![The flow of LoadTest investments](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/images/HTTPLoadTest-FullProcessFlow.png)


## Simulating "read" flow

A rule of thumb when it comes to define the steps in a read flow include the following questions
 - Is the data protected by authentication?  If so, create the "log-on" steps (I.e.  basic authentication, OAUTH2 or similar)
    Note: A simulated "read" might include HTTP Post operations, this is "normal"
 - What data is typically fetched when a user access the module or application. Create the steps to receive this information. 
    Note: If it likely that the steps above will be agressively cached by the application, add a step to the "write" flow to update and thus invalidate
    the cache.
       

### Quick overview of the key data concepts
![The LoadTest data structures](https://github.com/Cantara/HTTPLoadTest-Baseline/raw/master/images/HTTPLoadTest-DataStructures.png)
