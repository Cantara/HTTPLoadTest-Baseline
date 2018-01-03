package no.cantara.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadTestConfig {
    private String test_id;
    private String test_name;
    private int test_no_of_threads = 10;
    private int test_read_write_ratio = 90;

    private int test_sleep_in_ms = 10;
    private boolean test_randomize_sleeptime = false;
    private int test_duration_in_seconds = 40;


    public LoadTestConfig(@JsonProperty("test_id") String id,
                          @JsonProperty("test_name") String test_name,
                          @JsonProperty("test_no_of_threads") String test_no_of_threads,
                          @JsonProperty("test_read_write_ratio") String test_read_write_ratio,
                          @JsonProperty("test_sleep_in_ms") String test_sleep_in_ms,
                          @JsonProperty("test_randomize_sleeptime") String test_randomize_sleeptime,
                          @JsonProperty("test_duration_in_seconds") String test_duration_in_seconds) {
        this.test_id = id;
        this.test_name = test_name;
        this.test_no_of_threads = Integer.valueOf(test_no_of_threads);
        this.test_read_write_ratio = Integer.valueOf(test_read_write_ratio);
        this.test_sleep_in_ms = Integer.valueOf(test_sleep_in_ms);
        this.test_randomize_sleeptime = Boolean.parseBoolean(test_randomize_sleeptime);
        this.test_duration_in_seconds = Integer.valueOf(test_duration_in_seconds);

    }

    public LoadTestConfig() {
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

    public int getTest_no_of_threads() {
        return test_no_of_threads;
    }

    public void setTest_no_of_threads(int test_no_of_threads) {
        this.test_no_of_threads = test_no_of_threads;
    }

    public int getTest_read_write_ratio() {
        return test_read_write_ratio;
    }

    public void setTest_read_write_ratio(int test_read_write_ratio) {
        this.test_read_write_ratio = test_read_write_ratio;
    }

    public int getTest_sleep_in_ms() {
        return test_sleep_in_ms;
    }

    public void setTest_sleep_in_ms(int test_sleep_in_ms) {
        this.test_sleep_in_ms = test_sleep_in_ms;
    }

    public boolean isTest_randomize_sleeptime() {
        return test_randomize_sleeptime;
    }

    public void setTest_randomize_sleeptime(boolean test_randomize_sleeptime) {
        this.test_randomize_sleeptime = test_randomize_sleeptime;
    }

    public int getTest_duration_in_seconds() {
        return test_duration_in_seconds;
    }

    public void setTest_duration_in_seconds(int test_duration_in_seconds) {
        this.test_duration_in_seconds = test_duration_in_seconds;
    }

}