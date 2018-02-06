## Clustering HTTPLoadTest-Baseline

We are in process of extending HTTPLoadTest-Baseline to support clustered deployments. This functionality should be
considered to be in "early access" and to be used at your own risk. A HTTPLoadTest cluster will try to spread the load 
(in practise the number of configured threads) evenly among the registered nodes.


#### An example hazelcast.xml configuration for kubernetes deployments
```xml
<hazelcast xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.hazelcast.com/schema/config
                               http://www.hazelcast.com/schema/config/hazelcast-config-3.9.xsd"
           xmlns="http://www.hazelcast.com/schema/config">
    <properties>
        <!-- only necessary prior Hazelcast 3.8 -->
        <property name="hazelcast.discovery.enabled">true</property>
    </properties>

    <network>
        <join>
            <!-- deactivate normal discovery -->
            <multicast enabled="false"/>
            <tcp-ip enabled="false" />

            <!-- activate the Kubernetes plugin -->
            <discovery-strategies>
                <discovery-strategy enabled="true"
                                    class="com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategy">

                    <properties>
                        <!-- configure discovery service API lookup -->
                        <property name="service-dns">my-app.my-cluster01.svc.cluster.local</property>
                        <property name="service-dns-timeout">10</property>
                    </properties>
                </discovery-strategy>
            </discovery-strategies>
        </join>
    </network>
</hazelcast>
```