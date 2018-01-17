package no.cantara.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadTestBenchmark implements Serializable {
    private String benchmark_id;
    private String test_name;
    private int benchmark_req_sucessrate_percent = 99;

    private int benchmark_req_mean_read_duration_ms = 1010;
    private int benchmark_req_mean_write_duration_ms = 1020;
    private int benchmark_req_90percentile_read_duration_ms = 910;
    private int benchmark_req_90percentile_write_duration_ms = 920;


    private static final Logger log = LoggerFactory.getLogger(LoadTestBenchmark.class);

    public LoadTestBenchmark(@JsonProperty("benchmark_id") String id,
                             @JsonProperty("benchmark_name") String test_name,
                             @JsonProperty("benchmark_req_sucessrate_percent") String benchmark_req_sucessrate,
                             @JsonProperty("benchmark_req_mean_read_duration_ms") String benchmark_req_mean_read_duration_ms,
                             @JsonProperty("benchmark_req_mean_write_duration_ms") String benchmark_req_mean_write_duration_ms,
                             @JsonProperty("benchmark_req_90percentile_read_duration_ms") String benchmark_req_90percentile_read_duration_ms,
                             @JsonProperty("benchmark_req_90percentile_write_duration_ms") String benchmark_req_90percentile_write_duration_ms) {
        this.benchmark_id = id;
        this.test_name = test_name;
        this.benchmark_req_sucessrate_percent = Integer.parseInt(benchmark_req_sucessrate);
        this.benchmark_req_mean_read_duration_ms = Integer.parseInt(benchmark_req_mean_read_duration_ms);
        this.benchmark_req_mean_write_duration_ms = Integer.parseInt(benchmark_req_mean_write_duration_ms);
        this.benchmark_req_90percentile_read_duration_ms = Integer.parseInt(benchmark_req_90percentile_read_duration_ms);
        this.benchmark_req_90percentile_write_duration_ms = Integer.parseInt(benchmark_req_90percentile_write_duration_ms);

    }

    public LoadTestBenchmark() {
    }

    public String getBenchmark_id() {
        return benchmark_id;
    }

    public void setBenchmark_id(String benchmark_id) {
        this.benchmark_id = benchmark_id;
    }

    public String getTest_name() {
        return test_name;
    }

    public void setTest_name(String test_name) {
        this.test_name = test_name;
    }

    public int getBenchmark_req_sucessrate_percent() {
        return benchmark_req_sucessrate_percent;
    }

    public void setBenchmark_req_sucessrate_percent(int benchmark_req_sucessrate_percent) {
        this.benchmark_req_sucessrate_percent = benchmark_req_sucessrate_percent;
    }

    public int getBenchmark_req_mean_read_duration_ms() {
        return benchmark_req_mean_read_duration_ms;
    }

    public void setBenchmark_req_mean_read_duration_ms(int benchmark_req_mean_read_duration_ms) {
        this.benchmark_req_mean_read_duration_ms = benchmark_req_mean_read_duration_ms;
    }

    public int getBenchmark_req_mean_write_duration_ms() {
        return benchmark_req_mean_write_duration_ms;
    }

    public void setBenchmark_req_mean_write_duration_ms(int benchmark_req_mean_write_duration_ms) {
        this.benchmark_req_mean_write_duration_ms = benchmark_req_mean_write_duration_ms;
    }

    public int getBenchmark_req_90percentile_read_duration_ms() {
        return benchmark_req_90percentile_read_duration_ms;
    }

    public void setBenchmark_req_90percentile_read_duration_ms(int benchmark_req_90percentile_read_duration_ms) {
        this.benchmark_req_90percentile_read_duration_ms = benchmark_req_90percentile_read_duration_ms;
    }

    public int getBenchmark_req_90percentile_write_duration_ms() {
        return benchmark_req_90percentile_write_duration_ms;
    }

    public void setBenchmark_req_90percentile_write_duration_ms(int benchmark_req_90percentile_write_duration_ms) {
        this.benchmark_req_90percentile_write_duration_ms = benchmark_req_90percentile_write_duration_ms;
    }
}


