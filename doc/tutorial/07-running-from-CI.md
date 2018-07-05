# Example of embedding Httploadtest in your unit-tests

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
}```


* If you want to embed the Loadtests in your normal maven test, let us take a look at how that might be done [example of embedding Loadtest in maven test](./08-running-as-embedded-tests.md).
* With your automation completed, it's time to [explore advanced topics and use of HTTPLoadTest](./0-advanced-topics.md).