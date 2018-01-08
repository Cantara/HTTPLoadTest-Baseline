package no.cantara.service.model;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.testng.Assert.assertTrue;

public class LoadTestResultTest {

    private static List<LoadTestResult> unsafeList = new ArrayList<>();
    private static List<LoadTestResult> resultList = Collections.synchronizedList(unsafeList);
    private static final CsvMapper csvMapper = new CsvMapper();


    @Test
    public void testLoadTestResultCSVMapping() throws Exception {
        LoadTestResult loadTestResource = new LoadTestResult();
        resultList.add(loadTestResource);

        CsvSchema csvSchema = csvMapper.schemaFor(loadTestResource.getClass());

        if (true) {
            csvSchema = csvSchema.withHeader();
        } else {
            csvSchema = csvSchema.withoutHeader();
        }

        String csvstring = csvMapper.writer(csvSchema).writeValueAsString(resultList);
//        String csvstring2 = csvMapper.writeValueAsString(resultList);

        assertTrue(csvstring.contains("90"));
    }


    @Test
    public void testcalculateninetypercentile() {
        List<Long> times = new ArrayList<>();
        Random r = new Random();

        for (int n = 0; n < 100; n++) {
            times.add(r.nextLong());

        }
// add times

        Collections.sort(times);
        System.out.printf("The typical, 90%% and 99%%tile times were %,d / %,d / %,d %n",
                times.get(times.size() / 2), times.get(times.size() * 9 / 10), times.get(times.size() * 99 / 100));
    }
}