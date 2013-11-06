package org.jbpm.designer.expressioneditor.parser;

import org.jbpm.designer.expressioneditor.model.Condition;
import org.jbpm.designer.expressioneditor.model.ConditionExpression;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionEditorParser {

    public static final String VARIABLE_NAME_PARAM_REGEX = "[$_a-zA-Z][$_a-zA-Z0-9]*";

    private static Map<String, FunctionDef> functionsRegistry = new HashMap<String, FunctionDef>();

    private int parseIndex = 0;

    private String expression;

    static {

        //Operators for all types:

        FunctionDef isNull = new FunctionDef("isNull");
        isNull.addParam("param1", Object.class);
        functionsRegistry.put(isNull.getName(), isNull);

        //Global operators:

        FunctionDef equalsTo = new FunctionDef("equalsTo");
        equalsTo.addParam("param1", Object.class);
        equalsTo.addParam("param2", String.class);
        functionsRegistry.put(equalsTo.getName(), equalsTo);

        //Operators for String type:

        FunctionDef isEmpty = new FunctionDef("isEmpty");
        isEmpty.addParam("param1", Object.class);
        functionsRegistry.put(isEmpty.getName(), isEmpty);

        FunctionDef contains = new FunctionDef("contains");
        contains.addParam("param1", Object.class);
        contains.addParam("param2", String.class);
        functionsRegistry.put(contains.getName(), contains);

        FunctionDef startsWith = new FunctionDef("startsWith");
        startsWith.addParam("param1", Object.class);
        startsWith.addParam("param2", String.class);
        functionsRegistry.put(startsWith.getName(), startsWith);

        FunctionDef endsWith = new FunctionDef("endsWith");
        endsWith.addParam("param1", Object.class);
        endsWith.addParam("param2", String.class);
        functionsRegistry.put(endsWith.getName(), endsWith);

        // Operators for Numeric types:

        FunctionDef greaterThan = new FunctionDef("greaterThan");
        greaterThan.addParam("param1", Object.class);
        greaterThan.addParam("param2", String.class);
        functionsRegistry.put(greaterThan.getName(), greaterThan);

        FunctionDef greaterOrEqualThan = new FunctionDef("greaterOrEqualThan");
        greaterOrEqualThan.addParam("param1", Object.class);
        greaterOrEqualThan.addParam("param2", String.class);
        functionsRegistry.put(greaterOrEqualThan.getName(), greaterOrEqualThan);

        FunctionDef lessThan = new FunctionDef("lessThan");
        lessThan.addParam("param1", Object.class);
        lessThan.addParam("param2", String.class);
        functionsRegistry.put(lessThan.getName(), lessThan);

        FunctionDef lessOrEqualThan = new FunctionDef("lessOrEqualThan");
        lessOrEqualThan.addParam("param1", Object.class);
        lessOrEqualThan.addParam("param2", String.class);
        functionsRegistry.put(lessOrEqualThan.getName(), lessOrEqualThan);

        FunctionDef between = new FunctionDef("between");
        between.addParam("param1", Object.class);
        between.addParam("param2", String.class);
        between.addParam("param3", String.class);
        functionsRegistry.put(between.getName(), between);

        // Operators for Boolean type:

        FunctionDef isTrue = new FunctionDef("isTrue");
        isTrue.addParam("param1", Object.class);
        functionsRegistry.put(isTrue.getName(), isTrue);


        FunctionDef isFalse = new FunctionDef("isFalse");
        isFalse.addParam("param1", Object.class);
        functionsRegistry.put(isFalse.getName(), isFalse);

    }

    public ExpressionEditorParser(String expression) {
        this.expression = expression;
        this.parseIndex = expression != null ? 0 : -1;
    }

    public static void main(String args[]) {

        try {

            //todo move this to a JUnit test
            LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(new FileInputStream("/home/wmedvede/development/projects/droolsjbpm/jbpm-designer/jbpm-designer-backend/src/main/java/org/jbpm/designer/expressioneditor/parser/TestExpressions.txt")));
            String line = null;
            while ((line = lineReader.readLine()) != null) {
                System.out.println("line("+lineReader.getLineNumber()+") :" + line);

                ExpressionEditorParser parser = new ExpressionEditorParser(line);
                try {
                ConditionExpression conditionExpression = parser.parse();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int i = 0;
                /*

                System.out.println("1:" + parser.parseReturnSentence());
                System.out.println("2:" + parser.parseFunctionName());
                System.out.println("3:" + parser.parseVariableName());
                System.out.println("4:" + parser.parseParamDelimiter());
                System.out.println("5:" + parser.parseStringParameter());
                System.out.println("6:" + parser.parseParamDelimiter());
                System.out.println("7:" + parser.parseStringParameter());
                System.out.println("8:" + parser.parseParamDelimiter());
                System.out.println("9:" + parser.parseStringParameter());
                System.out.println("10:" + parser.parseFunctionClose());
                System.out.println("11:" + parser.parseSentenceClose());

                */


                //parser.parseASaco(line);
                //parser.parse(line);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public ConditionExpression parse() throws ParseException {

        ConditionExpression conditionExpression = new ConditionExpression();
        Condition condition = null;
        String functionName = null;
        FunctionDef functionDef = null;

        parseReturnSentence();

        functionName = parseFunctionName();
        functionDef = functionsRegistry.get(functionName);

        conditionExpression.setOperator(ConditionExpression.AND_OPERATOR);
        condition = new Condition(functionName);
        conditionExpression.getConditions().add(condition);

        String param = null;
        boolean first = true;

        for (ParamDef paramDef : functionDef.getParams()) {
            if (first) {
                first = false;
            } else {
                parseParamDelimiter();
            }

            if (Object.class.getName().equals(paramDef.getType().getName())) {
                param = parseVariableName();
            } else {
                param = parseStringParameter();
            }
            condition.addParam(param);
        }

        //all parameters were consumed
        parseFunctionClose();
        parseSentenceClose();


       return conditionExpression;
    }

    private String parseReturnSentence() throws ParseException {

        int index = nextNonBlank();
        if (index < 0) throw new ParseException("return sentence was not found.", parseIndex);

        if (!expression.startsWith("return ", index)) {
            //the expression does not start with return.
            throw new ParseException("return sentence was not found.", parseIndex);
        }

        parseIndex = index + "return ".length();

        return "return ";
    }

    private String parseFunctionName() throws ParseException {

        int index = nextNonBlank();
        if (index < 0) throw new ParseException("function call not found", parseIndex);

        String functionName = null;

        for(FunctionDef functionDef : functionsRegistry.values()) {
            if (expression.startsWith(functionDef.getName()+"(", index)) {
                functionName = functionDef.getName();
                break;
            }
        }

        if (functionName == null) throw new ParseException("function call not found", parseIndex);

        parseIndex = index + functionName.length() +1;

        return functionName;
    }

    private String parseFunctionClose() throws ParseException {
        int index = nextNonBlank();
        if (index < 0) throw new ParseException("function call not closed properly", parseIndex);

        if (expression.charAt(index) != ')') throw new ParseException("function call not closed properly", parseIndex);

        parseIndex = index +1;
        return ")";
    }

    private String parseSentenceClose() throws ParseException {
        int index = nextNonBlank();
        if (index < 0) throw new ParseException("sentence not closed properly", parseIndex);

        if (expression.charAt(index) != ';') throw new ParseException("sentence not closed properly", parseIndex);

        parseIndex = index +1;
        while (parseIndex < expression.length()) {
            if (!isBlank(expression.charAt(parseIndex))) throw new ParseException("sentence not closed properly", parseIndex);
        }

        return ";";
    }

    private String parseVariableName() throws ParseException {
        int index = nextNonBlank();
        if (index < 0) throw new ParseException("variableName not found", parseIndex);

        Pattern variableNameParam = Pattern.compile(VARIABLE_NAME_PARAM_REGEX);
        Matcher variableMatcher = variableNameParam.matcher(expression.substring(index, expression.length()));

        String variableName = null;
        if (variableMatcher.find()) {
            variableName = variableMatcher.group();
        } else {
            throw new ParseException("variableName not found", parseIndex);
        }

        parseIndex = index + variableName.length();

        return variableName;
    }

    private String parseParamDelimiter() throws ParseException {
        int index = nextNonBlank();
        if (index < 0) throw new ParseException("parameter delimiter not found", parseIndex);

        if (expression.charAt(index) != ',') {
            throw new ParseException("parameter delimiter not found", parseIndex);
        }

        parseIndex = index + 1;
        return ",";
    }


    private String parseStringParameter() throws ParseException {
        int index = nextNonBlank();
        if (index < 0) throw new ParseException("string parameter not found", parseIndex);

        if (expression.charAt(index) != '"') {
            throw new ParseException("string parameter delimiter not found", parseIndex);
        }

        int shift = 1;
        Character scapeChar = Character.valueOf('\\');
        Character last = null;
        boolean strReaded = false;
        StringBuilder param = new StringBuilder();
        for (int i = index+1; i < expression.length(); i++) {
            if (expression.charAt(i) == '\\') {
                last = expression.charAt(i);
            } else if (expression.charAt(i) != '"') {
                if (last != null) {
                    shift++;
                    param.append(last);
                }
                last = null;
                shift++;
                param.append(expression.charAt(i));
            } else {
                if (scapeChar.equals(last)) {
                    shift += 2;
                    param.append('"');
                    last = null;
                } else {
                    shift++;
                    strReaded = true;
                    break;
                }
            }
        }

        if (!strReaded) throw new ParseException("string parameter not found", parseIndex);

        parseIndex = index + shift;
        return param.toString();
    }

    private int nextNonBlank() {
        if (parseIndex < 0) return -1;

        for (int i = parseIndex; i < expression.length(); i++) {
            if (!isBlank(expression.charAt(i))) {
                return i;
            }
        }

        return -1;
    }

    private boolean isBlank(Character character) {
        return character != null && (character.equals('\n') || character.equals(' '));
    }

}