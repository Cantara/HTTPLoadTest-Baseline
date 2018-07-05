
### HTTPLoadTest, embeding loadtests

Belov is an example from someone embedding HTTPLoadtest in their Java (testng) tests.

```java
@Listeners(TestServerListener.class)
public class InMemoryLoadtest {

    @Inject
    TestServer server;

    @Test
    public void thatInMemoryConfigurationIsSane() {
        /*
         * Given
         */
        Set<String> managedDomains = Specification.getInstance().getJsonSchema().getDefinitions().keySet();

        /*
         * Pre-populate data and generate matching read and write specifications
         */
        LoadtestHelper helper = new LoadtestHelper();
        helper.prePopulateData(managedDomains);
        helper.generateSpecifications(managedDomains, server.getTestServerServicePort());

        /*
         * Run load-test
         */
        String testResult = server.loadtest().runFromJsonWithDefaultLifecycle(
                helper.getReadSpecification(),
                helper.getWriteSpecification(),
                CommonUtils.readFileAsUtf8("loadtest/inmem/configurations/InMemLoadTestConfig.json")
        );

        /*
         * Present results
         */
        System.out.println(testResult);
    }
}
```


* With your automation completed, it's time to [explore advanced topics and use of HTTPLoadTest](./09-advanced-topics.md).