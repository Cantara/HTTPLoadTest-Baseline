package no.cantara.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"test_id", "test_name", "test_run_no", "test_duration", "test_success", "test_deviation_flag", "worker_concurrency_degree", "command_concurrency_degree", "test_tags"})
public class LoadTestResult implements Serializable {
    private String test_id;
    private String test_name;
    private String test_tags;
    private long test_timestamp = System.currentTimeMillis();
    private int test_run_no = 90;

    private double test_duration;
    private boolean test_success = false;
    private boolean test_deviation_flag = false;
    private static final Logger log = LoggerFactory.getLogger(LoadTestResult.class);

    private int worker_concurrency_degree;
    private int command_concurrency_degree;


    public LoadTestResult(@JsonProperty("test_id") String id,
                          @JsonProperty("test_name") String test_name,
                          @JsonProperty("test_tags") String test_tags,
                          @JsonProperty("test_run_no") String test_run_no,
                          @JsonProperty("test_duration") String test_duration,
                          @JsonProperty("test_success") String test_success,
                          @JsonProperty("test_deviation_flag") String test_deviation_flag,
                          @JsonProperty("worker_concurrency_degree") String worker_concurrency_degree,
                          @JsonProperty("command_concurrency_degree") String command_concurrency_degree) {
        this.test_id = id;
        this.test_name = test_name;
        this.test_tags = test_tags;
        this.test_run_no = Integer.parseInt(test_run_no);
        this.test_duration = Double.parseDouble(test_duration);
        this.test_success = Boolean.parseBoolean(test_success);
        this.test_deviation_flag = Boolean.parseBoolean(test_deviation_flag);
        this.worker_concurrency_degree = Integer.parseInt(worker_concurrency_degree);
        this.command_concurrency_degree = Integer.parseInt(command_concurrency_degree);

    }

    public LoadTestResult() {
    }

    public String getTest_id() {
        return test_id;
    }

    public void setTest_id(String test_id) {
        this.test_id = test_id;
    }

    public String getTest_name() {
        return test_name;
    }

    public void setTest_name(String test_name) {
        this.test_name = test_name;
    }

    public String getTest_tags() {
        return test_tags;
    }

    public void setTest_tags(String test_tags) {
        this.test_tags = test_tags;
    }

    public long getTest_timestamp() {
        return test_timestamp;
    }

    public void setTest_timestamp(long test_timestamp) {
        this.test_timestamp = test_timestamp;
    }

    public int getTest_run_no() {
        return test_run_no;
    }

    public void setTest_run_no(int test_run_no) {
        this.test_run_no = test_run_no;
    }

    public double getTest_duration() {
        return test_duration;
    }

    public void setTest_duration(double test_duration) {
        this.test_duration = test_duration;
    }

    public boolean isTest_success() {
        return test_success;
    }

    public void setTest_success(boolean test_success) {
        this.test_success = test_success;
    }

    public boolean isTest_deviation_flag() {
        return test_deviation_flag;
    }

    public void setTest_deviation_flag(boolean test_deviation_flag) {
        this.test_deviation_flag = test_deviation_flag;
    }

    public int getWorker_concurrency_degree() {
        return worker_concurrency_degree;
    }

    public void setWorker_concurrency_degree(int worker_concurrency_degree) {
        this.worker_concurrency_degree = worker_concurrency_degree;
    }

    public int getCommand_concurrency_degree() {
        return command_concurrency_degree;
    }

    public void setCommand_concurrency_degree(int command_concurrency_degree) {
        this.command_concurrency_degree = command_concurrency_degree;
    }
}

