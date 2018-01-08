package no.cantara.service.model;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        assertTrue(csvstring.contains("56"));
    }


}