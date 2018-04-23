package no.cantara.service.loadtest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.base.util.json.JsonPathHelper;
import no.cantara.base.util.xml.XpathHelper;
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
    private static final ObjectMapper mapper = new ObjectMapper();


    public static Map<String, String> parse(String resultToParse, Map<String, String> replacementpaths) {
        Map<String, String> resultsMap = new HashMap<>();
        if (resultToParse == null) {
            log.trace("resultToParse was empty, so returning empty .");
            return resultsMap;
        }
        for (String pathKey : replacementpaths.keySet()) {
            String path = replacementpaths.get(pathKey);
            if (path.startsWith("$")) {
                resultsMap = parseWithJsonPath(resultToParse, replacementpaths);
                break;
            } else if (path.startsWith("/")) {
                resultsMap = parseWithXPath(resultToParse, replacementpaths);
                break;
            } else {
                //return parseWithRegexp(resultToParse,replacementpaths);
            }
        }
        return resultsMap;
    }

    public static Map parseWithRegexp(String resultToParse, Map<String, String> regexpSelectorMap) {
        Map<String, String> resultsMap = new HashMap<>();
        if (resultToParse == null) {
            log.trace("resultToParse was empty, so returning empty .");
            return resultsMap;
        }
        log.info("parseWithRegexp {}", resultToParse);
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
        if (resultToParse == null || resultToParse.length() < 1 || jsonpaths == null || resultToParse.startsWith("StatusCode:")) {
            log.trace("resultToParse was empty, so returning empty .");
            return resultsMap;
        }
        log.info("parseWithJsonPath {}", resultToParse);

        for (String jsonPathKey : jsonpaths.keySet()) {
            try {
                List<String> resultStrings = JsonPathHelper.findJsonpathList(resultToParse, jsonpaths.get(jsonPathKey));

                if (resultStrings == null || resultStrings.size() == 0) {
                    log.debug("jsonpath returned zero hits");
                    //break;
                } else {
                    String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultStrings);
                    //String result = resultStrings.toString();
                    resultsMap.put(jsonPathKey, result);
//                    resultsMap.put(jsonPathKey, result.substring(2, result.length() - 2));
                }
            } catch (Exception e) {
                log.warn("Error in trying to match variables from result. jspnpath:{} - result: {}", jsonPathKey, resultToParse);
            }
        }
        return resultsMap;
    }

    public static Map<String, String> parseWithXPath(String resultToParse, Map<String, String> xpaths) {
        Map<String, String> resultsMap = new HashMap<>();
        if (resultToParse == null || resultToParse.length() < 1 || xpaths == null || resultToParse.startsWith("StatusCode:")) {
            log.trace("resultToParse was empty, so returning empty .");
            return resultsMap;
        }

        log.info("parseWithJsonPath {}", resultToParse);
        for (String xPathKey : xpaths.keySet()) {
            try {
                String result =
                        XpathHelper.findValue(resultToParse, xpaths.get(xPathKey));

                if (result == null || result.length() < 1) {
                    log.debug("xpath returned zero hits");
                    //break;
                } else {
                    resultsMap.put(xPathKey, result);
//                    resultsMap.put(jsonPathKey, result.substring(2, result.length() - 2));
                }
            } catch (Exception e) {
                log.warn("Error in trying to match variables from result. xpaths:{} - result: {}", xPathKey, resultToParse);
            }
        }
        return resultsMap;
    }

    public static String first50(String s) {
        if (s == null) {
            return s;
        }
        if (s.length() > 50) {
            return s.substring(0, 50);
        }
        return s;
    }

    public static String first150(String s) {
        if (s == null) {
            return s;
        }
        if (s.length() > 150) {
            return s.substring(0, 150);
        }
        return s;
    }

    public static String firstX(String s, int x) {
        if (s == null) {
            return s;
        }
        if (s.length() > x) {
            return s.substring(0, x);
        }
        return s;
    }
}
