package no.cantara.service.loadtest;

import no.cantara.base.util.json.JsonPathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPResultUtil {
    private static final Logger log = LoggerFactory.getLogger(HTTPResultUtil.class);

    public static Map parseWithRegexp(String exampleResult, Map<String, String> regexpSelectorMap) {
        Map<String, String> results = new HashMap<>();


        return results;
    }

    public static Map parseWithJsonPath(String resultToParse, Map<String, String> jsonpaths) {
        Map<String, String> resultsMap = new HashMap<>();
        if (resultToParse == null) {
            log.trace("getApplicationNamesFromApplicationsJson was empty, so returning empty .");
            return resultsMap;
        }

        for (String jsonPathKey : jsonpaths.keySet()) {
            List<String> resultStrings = JsonPathHelper.findJsonpathList(resultToParse, jsonpaths.get(jsonPathKey));
            if (resultStrings == null) {
                log.debug("jsonpath returned zero hits");
                break;
            }
            resultsMap.put(jsonPathKey, resultStrings.toString());

        }
        return resultsMap;
    }
}
