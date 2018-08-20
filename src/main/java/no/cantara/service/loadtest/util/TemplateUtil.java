package no.cantara.service.loadtest.util;

import no.cantara.service.loadtest.Fizzler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateUtil {

    private static final Logger log = LoggerFactory.getLogger(TemplateUtil.class);

    interface FizzleFunction {
        String apply(String parameters, String input);
    }

    private static final Map<String, FizzleFunction> fizzleFunctionByKey = new LinkedHashMap<>();

    static {
        // Use only lowercase keys in order to support case-insensitive matching
        fizzleFunctionByKey.put("chars", (parameters, input) -> Fizzler.getRandomCharacters(input.length()));
        fizzleFunctionByKey.put("digits", (parameters, input) -> Fizzler.getRandomDigits(input.length()));
        fizzleFunctionByKey.put("u_chars", (parameters, input) -> Fizzler.getRandomUppercaseCharacter(input.length()));
        fizzleFunctionByKey.put("l_chars", (parameters, input) -> Fizzler.getRandomLowercaseCharacter(input.length()));
        fizzleFunctionByKey.put("hex", (parameters, input) -> Fizzler.getRandomHEXCharacter(input.length()));
        fizzleFunctionByKey.put("option", (parameters, input) -> Fizzler.getRandomSetValue(input));
        fizzleFunctionByKey.put("optionvalue", (parameters, input) -> Fizzler.getRandomSetValueAsString(input));
        fizzleFunctionByKey.put("substring", (parameters, input) -> Fizzler.getSubString(parameters, input));
    }

    static final Pattern variablePattern = Pattern.compile("#\\(?(\\p{Alnum}+)\\)?");

    public static String updateTemplateWithValuesFromMap(String template, Map<String, String> templatereplacementMap) {
        if (template == null) {
            return "";
        }

        // prepare build expression-map, no resolution or evaluation done here
        Map<String, Expression> expressionByKey = new LinkedHashMap<>();
        if (templatereplacementMap != null) {
            for (Map.Entry<String, String> e : templatereplacementMap.entrySet()) {
                Matcher m = variablePattern.matcher(e.getKey());
                if (m.matches()) {
                    String variableIdentifier = m.group(1).toLowerCase();
                    expressionByKey.put(variableIdentifier, new Expression(e.getValue()));
                } else {
                    log.warn("template-replacement-map contains key not on #variable form: " + e.getKey());
                }
            }
        }

        // resolve expressions in template recursively and lazily
        String result = new Expression(template).resolve(expressionByKey);

        return result;
    }

    static class Expression {

        static final Pattern fizzleFunctionPattern =
                Pattern.compile("#[Ff][Ii][Zz][Zz][Ll][Ee]\\(([^():]+)(?:\\(([^)]*)\\))?:?([^)]*)\\)");
        static final Pattern replaceablePattern = Pattern.compile("(?:" + fizzleFunctionPattern.pattern() + ")|(?:" + variablePattern.pattern() + ")");

        final String template;
        String resolvedExpression;

        Expression(String template) {
            this.template = template;
        }

        String resolve(Map<String, Expression> expressionByKey) {
            if (resolvedExpression != null) {
                return resolvedExpression;
            }

            resolvedExpression = "$$circular$$reference$$protection$$";

            StringBuilder result = new StringBuilder();
            Matcher replaceableExpressionsInTemplateMatcher = replaceablePattern.matcher(template);
            int previousEnd = 0;
            while (replaceableExpressionsInTemplateMatcher.find()) {
                result.append(template, previousEnd, replaceableExpressionsInTemplateMatcher.start());
                if (replaceableExpressionsInTemplateMatcher.group(1) != null) {
                    String fizzleFunctionKey = replaceableExpressionsInTemplateMatcher.group(1).toLowerCase();
                    String fizzleFunctionArguments = replaceableExpressionsInTemplateMatcher.group(2);
                    String fizzleInput = replaceableExpressionsInTemplateMatcher.group(3);
                    FizzleFunction function = fizzleFunctionByKey.get(fizzleFunctionKey);
                    String resolvedFizzleInput = new Expression(fizzleInput).resolve(expressionByKey);
                    String fizzleOutput = function.apply(fizzleFunctionArguments, resolvedFizzleInput);
                    result.append(fizzleOutput);
                } else if (replaceableExpressionsInTemplateMatcher.group(4) != null) {
                    String variableIdentifier = replaceableExpressionsInTemplateMatcher.group(4).toLowerCase();
                    Expression expression = expressionByKey.get(variableIdentifier);
                    if (expression == null) {
                        log.warn("Unable to resolve template variable #" + variableIdentifier);
                        result.append(expression);
                    } else {
                        String resolvedExpression = expression.resolve(expressionByKey);
                        result.append(resolvedExpression);
                        log.trace("Replaced #{} with: {}", variableIdentifier, resolvedExpression);
                    }
                }
                previousEnd = replaceableExpressionsInTemplateMatcher.end();
            }
            result.append(template.substring(previousEnd)); // tail

            resolvedExpression = result.toString();
            return resolvedExpression;
        }
    }
}
