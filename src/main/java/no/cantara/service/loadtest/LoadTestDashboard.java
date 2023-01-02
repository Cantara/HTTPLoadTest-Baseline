package no.cantara.service.loadtest;

import no.cantara.service.health.HealthResource;

import static no.cantara.service.Main.CONTEXT_PATH;
import static no.cantara.service.config.ConfigLoadTestResource.CONFIG_PATH;
import static no.cantara.service.config.ConfigLoadTestResource.backgroundImageURL;
import static no.cantara.service.health.HealthResource.HEALTH_PATH;

/**
 * LoadTestDashboard class helps to generate HTML page with a pre-configured ChartJS
 */
public class LoadTestDashboard {

    /**
     * Page title
     */
    private String pageTitle = "HTTPLoadTest - Dashboard";

    /**
     * Height of the chart layer
     */
    private String chartHeight = "40vh";

    /**
     * Interval between update requests in milliseconds
     */
    private int updateInterval = 5000;

    /**
     * Type of throughput chart
     */
    private String throughputChartType = "'line'";

    /**
     * Title of throughput chart
     */
    private String throughputTitle = "Throughput";

    /**
     * Title of throughput chart
     */
    private String throughputPointStyle = "false";

    /**
     * Main color of throughput chart
     */
    private String throughputColor = "rgb(255, 0, 0)";

    /**
     * Background color of throughput chart
     */
    private String throughputBackgroundColor = "rgba(255, 0, 0, 0.5)";

    /**
     * Type of latency chart
     */
    private String latencyChartType = "'line'";

    /**
     * Type of latency chart
     */
    private String latencyTitle = "Latency";

    /**
     * Title of latency chart
     */
    private String latencyPointStyle = "false";

    /**
     * Main color of latency chart
     */
    private String latencyColor = "rgb(0, 0, 255)";

    /**
     * Background color of latency chart
     */
    private String latencyBackgroundColor = "rgba(0, 0, 255, 0.5)";

    /**
     * Url to ChartJS library
     */
    private String chartLibraryUrl = "https://cdn.jsdelivr.net/npm/chart.js";

    /**
     * Is the process in active status
     */
    private final boolean isRunning;

    /**
     * Constructor
     *
     * @param isRunning if true - activate periodic updates
     */
    public LoadTestDashboard(final boolean isRunning) {
        this.isRunning = isRunning;
    }

    /**
     * Sets the title of the page.
     * Default: "HTTPLoadTest - Dashboard"
     *
     * @param pageTitle new title
     * @return current object
     */
    public LoadTestDashboard setPageTitle(final String pageTitle) {
        this.pageTitle = pageTitle;
        return this;
    }

    /**
     * Sets the height of the chart
     * Default: "40vh"
     *
     * @param height new height
     * @return current object
     */
    public LoadTestDashboard setChartHeight(final String height) {
        this.chartHeight = height;
        return this;
    }

    /**
     * Sets the update interval in milliseconds
     * Default: 5000
     *
     * @param interval new interval
     * @return current object
     */
    public LoadTestDashboard setUpdateInterval(final int interval) {
        this.updateInterval = interval;
        return this;
    }

    /**
     * Sets the type of the throughput chart
     * Default: "'line'"
     *
     * @param type new chart type
     * @return current object
     */
    public LoadTestDashboard setThroughputChartType(final String type) {
        this.throughputChartType = type;
        return this;
    }

    /**
     * Sets the style of the throughput chart
     * Default: "false"
     *
     * @param style new chart style
     * @return current object
     */
    public LoadTestDashboard setThroughputPointStyle(final String style) {
        this.throughputPointStyle = style;
        return this;
    }

    /**
     * Sets the title of the throughput chart
     * Default: "Throughput"
     *
     * @param title new chart style
     * @return current object
     */
    public LoadTestDashboard setThroughputTitle(final String title) {
        this.throughputTitle = title;
        return this;
    }

    /**
     * Sets the color of the line in throughput chart
     * Default: "rgb(255, 0, 0)"
     *
     * @param color new chart style
     * @return current object
     */
    public LoadTestDashboard setThroughputColor(final String color) {
        this.throughputColor = color;
        return this;
    }

    /**
     * Sets the color of the background-line in throughput chart
     * Default: "rgba(255, 0, 0, 0.5)"
     *
     * @param color new chart style
     * @return current object
     */
    public LoadTestDashboard setThroughputBackgroundColor(final String color) {
        this.throughputBackgroundColor = color;
        return this;
    }

    /**
     * Sets the type of the latency chart
     * Default: "'line'"
     *
     * @param type new chart type
     * @return current object
     */
    public LoadTestDashboard setLatencyChartType(final String type) {
        this.latencyChartType = type;
        return this;
    }

    /**
     * Sets the style of the latency chart
     * Default: "false"
     *
     * @param style new chart style
     * @return current object
     */
    public LoadTestDashboard setLatencyPointStyle(final String style) {
        this.latencyPointStyle = style;
        return this;
    }

    /**
     * Sets the title of the latency chart
     * Default: "Latency"
     *
     * @param title new chart style
     * @return current object
     */
    public LoadTestDashboard setLatencyTitle(final String title) {
        this.latencyTitle = title;
        return this;
    }

    /**
     * Sets the color of the line in latency chart
     * Default: "rgb(0, 0, 255)"
     *
     * @param color new chart style
     * @return current object
     */
    public LoadTestDashboard setLatencyColor(final String color) {
        this.latencyColor = color;
        return this;
    }

    /**
     * Sets the color of the background-line in latency chart
     * Default: "rgba(0, 0, 255, 0.5)"
     *
     * @param color new chart style
     * @return current object
     */
    public LoadTestDashboard setLatencyBackgroundColor(final String color) {
        this.latencyBackgroundColor = color;
        return this;
    }

    /**
     * Sets the URL to ChartJS library
     * Default: <a href="https://cdn.jsdelivr.net/npm/chart.js">"https://cdn.jsdelivr.net/npm/chart.js"</a>
     *
     * @param url new url to library
     * @return current object
     */
    public LoadTestDashboard setChartLibraryUrl(final String url) {
        this.chartLibraryUrl = url;
        return this;
    }

    /**
     * Generates HTML page
     *
     * @return generated page
     */
    public String generateHTMLPage() {
        StringBuilder result = new StringBuilder();

        String throughputDiv = "<div style=\"height:" + chartHeight + "\"><canvas id=\"throughputDashboard\"></canvas></div>";
        String latencyDiv = "<div style=\"height:" + chartHeight + "\"><canvas id=\"latencyDashboard\"></canvas></div>";
        String chartScript = "<script src=\"" + chartLibraryUrl + "\"></script>";
        String updateTimer = "<script>const updateInterval = setInterval(updateStatus, " + updateInterval + ");</script>";
        String recordsCount = "<script>var recordsCount = -1;</script>";
        String initCall = "<script>updateStatus();</script>";

        // Add header
        result.append(getHTMLStartPre());

        // Add ChartJS scripts
        result.append(chartScript);
        result.append(recordsCount);

        // Add throughput chart div with data
        result.append(throughputDiv);
        result.append(generateThroughputData());
        result.append(generateThroughputContext());

        // Add latency chart div with data
        result.append(latencyDiv);
        result.append(generateLatencyData());
        result.append(generateLatencyContext());

        // Add footer
        result.append(generateFetchStatusMethod());

        // Add a timer (interval) if the test process is running
        if (isRunning) {
            result.append(updateTimer);
        }

        // Add data initialization - request to full status endpoint
        result.append(initCall);

        // Add footer
        result.append(getHTMLEndPre());

        return result.toString();
    }

    /**
     * Generates header
     *
     * @return generated header
     */
    private String getHTMLStartPre() {
        return "<html>" +
                "<head>" +
                "  <meta charset=\"UTF-8\">" +
                "</head>  " +
                "<body background=\"" + backgroundImageURL + "\">" +
                "  <h3>" + pageTitle + "</h3><br/>" +
                "  <ul>" +
                "  <li><b><a href=\"" + CONTEXT_PATH + CONFIG_PATH + "\">LoadTestConfig</a></b></li>" +
                "  <li><a href=\"" + CONTEXT_PATH + HEALTH_PATH + "\">Health</a></li>" +
                "  </ul><br/>" +
                "  <pre>";
    }

    /**
     * Generates footer
     *
     * @return generated footer
     */
    private String getHTMLEndPre() {
        return "  </pre><br/><br/>" +
                "  <a href=\"https://github.com/Cantara/HTTPLoadTest-Baseline\">Documentation and SourceCode</a><br/><br/>" +
                "  HTTPLoadTest-Baseline " + HealthResource.getVersion() + "<br/" +
                "  </body>" +
                "</html>";
    }

    /**
     * Generates throughputData constant for throughput chart
     *
     * @return generated throughputData
     */
    private String generateThroughputData() {
        return "<script>" +
                "const throughputData = {" +
                "  labels: []," +
                "  datasets: [" +
                "    {" +
                "      label: '" + throughputTitle + "'," +
                "      data: []," +
                "      pointStyle: " + throughputPointStyle + "," +
                "      borderColor: '" + throughputColor + "'," +
                "      backgroundColor: '" + throughputBackgroundColor + "'," +
                "    }," +
                "  ]" +
                "};" +
                "</script>";
    }

    /**
     * Generates latencyData constant for latency chart
     *
     * @return generated latencyData
     */
    private String generateLatencyData() {
        return "<script>" +
                "const latencyData = {" +
                "  labels: []," +
                "  datasets: [" +
                "    {" +
                "      label: '" + latencyTitle + "'," +
                "      data: []," +
                "      pointStyle: " + latencyPointStyle + "," +
                "      borderColor: '" + latencyColor + "'," +
                "      backgroundColor: '" + latencyBackgroundColor + "'," +
                "    }" +
                "  ]" +
                "};" +
                "</script>";
    }

    /**
     * Generates and initializes throughput context and chart
     *
     * @return generated throughput chart
     */
    private String generateThroughputContext() {
        return "<script>" +
                "const throughputCtx = document.getElementById('throughputDashboard');" +
                "const throughputChart = new Chart(throughputCtx, {" +
                "    type: " + throughputChartType + "," +
                "    data: throughputData," +
                "    options: {" +
                "      responsive: true," +
                "      maintainAspectRatio: false," +
                "      scales: {" +
                "        x: {" +
                "          display: true," +
                "          title: {" +
                "            display: true," +
                "            text: 'Seconds'" +
                "          }" +
                "        }," +
                "        y: {" +
                "          display: true," +
                "          title: {" +
                "            display: true," +
                "            text: 'Messages'" +
                "          }," +
                "        }" +
                "      }," +
                "      plugins: {" +
                "        legend: {" +
                "          position: 'top'," +
                "        }," +
                "        title: {" +
                "          display: false," +
                "        }," +
                "      }" +
                "    }" +
                "  });" +
                "</script>";
    }

    /**
     * Generates and initializes latency context and chart
     *
     * @return generated latency chart
     */
    private String generateLatencyContext() {
        return "<script>" +
                "const latencyCtx = document.getElementById('latencyDashboard');" +
                "const latencyChart = new Chart(latencyCtx, {" +
                "    type: " + latencyChartType + "," +
                "    data: latencyData," +
                "    options: {" +
                "      responsive: true," +
                "      maintainAspectRatio: false," +
                "      scales: {" +
                "        x: {" +
                "          display: true," +
                "          title: {" +
                "            display: true," +
                "            text: 'Requests'" +
                "          }" +
                "        }," +
                "        y: {" +
                "          display: true," +
                "          title: {" +
                "            display: true," +
                "            text: 'Duration in ms'" +
                "          }," +
                "        }" +
                "      }," +
                "      plugins: {" +
                "        legend: {" +
                "          position: 'top'," +
                "        }," +
                "        title: {" +
                "          display: false," +
                "        }," +
                "      }" +
                "    }" +
                "  });" +
                "</script>";
    }

    /**
     * Generates an update request method
     *
     * @return generated updateStatus method
     */
    private String generateFetchStatusMethod() {
        return "<script>updateStatus = function() {" +
                "latencyChart.data.labels = [];" +
                "latencyChart.data.datasets[0].data = [];" +
                "throughputChart.data.labels = [];" +
                "throughputChart.data.datasets[0].data = [];" +
                "throughputArray = [];" +
                "fetch('" + CONTEXT_PATH + LoadTestResource.APPLICATION_PATH_FULLSTATUS + "')" +
                "  .then((response) => response.json())" +
                "  .then((data) => {" +
                "     data.sort((a,b) => a.test_run_no - b.test_run_no).forEach((val) => {" +
                "       latencyChart.data.labels.push(val.test_run_no);" +
                "       latencyChart.data.datasets[0].data.push(val.test_duration);" +
                "       var time = new Date(val.test_timestamp + val.test_duration).toLocaleTimeString(\"en-US\", { hour12: false });" +
                "       throughputArray[time] = throughputArray[time] != null ? throughputArray[time] + 1 : 1;" +
                "     });" +
                "     if (recordsCount != -1 && data.length == recordsCount) {clearInterval(updateInterval);}" +
                "     for (var key in throughputArray) {throughputChart.data.labels.push(key); throughputChart.data.datasets[0].data.push(throughputArray[key]);}" +
                "     latencyChart.update();" +
                "     throughputChart.update();" +
                "     recordsCount = data.length;" +
                "   });" +
                "};" +
                "</script>";
    }

}
