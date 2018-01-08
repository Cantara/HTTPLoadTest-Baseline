package no.cantara.service.loadtest.util;

import no.cantara.base.util.json.JsonPathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPResultUtil {
    private static final Logger log = LoggerFactory.getLogger(HTTPResultUtil.class);


    public static Map parseWithRegexp(String resultToParse, Map<String, String> regexpSelectorMap) {
        Map<String, String> resultsMap = new HashMap<>();
        if (resultToParse == null) {
            log.trace("resultToParse was empty, so returning empty .");
            return resultsMap;
        }
        for (String regExpKey : regexpSelectorMap.keySet()) {
            Pattern MY_PATTERN = Pattern.compile(regexpSelectorMap.get(regExpKey));
            Matcher m = MY_PATTERN.matcher(resultToParse);
            List<String> resultStrings = new ArrayList<>();
            while (m.find()) {
                String s = m.group(1);
                resultStrings.add(s);
                // s now contains "BAR"
            }
            if (resultStrings == null) {
                log.debug("regexp returned zero hits");
                break;
            }
            resultsMap.put(regExpKey, resultStrings.toString());
        }

        return resultsMap;
    }

    public static Map<String, String> parseWithJsonPath(String resultToParse, Map<String, String> jsonpaths) {
        Map<String, String> resultsMap = new HashMap<>();
        if (resultToParse == null || jsonpaths == null || resultToParse.startsWith("StatusCode:")) {
            log.trace("resultToParse was empty, so returning empty .");
            return resultsMap;
        }

        for (String jsonPathKey : jsonpaths.keySet()) {
            try {
                List<String> resultStrings = JsonPathHelper.findJsonpathList(resultToParse, jsonpaths.get(jsonPathKey));
                if (resultStrings == null || resultStrings.size() == 0) {
                    log.debug("jsonpath returned zero hits");
                    //break;
                } else {
                    String result = resultStrings.toString();
                    resultsMap.put(jsonPathKey, result.substring(2, result.length() - 2));
                }
            } catch (Exception e) {
                log.warn("Error in trying to match variables from result. jspnpath:{} - result: {}", jsonPathKey, resultToParse);
            }
        }
        return resultsMap;
    }
}
