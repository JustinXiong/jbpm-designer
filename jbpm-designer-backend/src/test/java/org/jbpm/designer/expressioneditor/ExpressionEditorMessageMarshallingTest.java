package org.jbpm.designer.expressioneditor;

import org.jbpm.designer.expressioneditor.marshalling.ExpressionEditorMessageJSONMarshaller;
import org.jbpm.designer.expressioneditor.marshalling.ExpressionEditorMessageJSONUnmarshaller;
import org.jbpm.designer.expressioneditor.model.Condition;
import org.jbpm.designer.expressioneditor.model.ExpressionEditorMessage;
import org.jbpm.designer.expressioneditor.model.ConditionExpression;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;


public class ExpressionEditorMessageMarshallingTest {

    @Test
    public void testUnmarshalling() throws Exception {

        ExpressionEditorMessageJSONUnmarshaller unmarshaller = new ExpressionEditorMessageJSONUnmarshaller();
        ExpressionEditorMessage message = unmarshaller.unmarshall(ExpressionEditorMessageMarshallingTest.class.getResourceAsStream("condition_editor_message.json"));

        assertNotNull("json message was not marshalled", message);
        assertNotNull("condition expression was not marshalled", message.getExpression());

        assertEquals("OR", message.getExpression().getOperator());
        assertEquals(2, message.getExpression().getConditions().size());

        assertEquals("isEquals", message.getExpression().getConditions().get(0).getFunction());
        assertEquals("variableName1", message.getExpression().getConditions().get(0).getParameters().get(0));
        assertEquals("param1.1", message.getExpression().getConditions().get(0).getParameters().get(1));

        assertEquals("isBetween", message.getExpression().getConditions().get(1).getFunction());
        assertEquals("variableName2", message.getExpression().getConditions().get(1).getParameters().get(0));
        assertEquals("param2.1", message.getExpression().getConditions().get(1).getParameters().get(1));
        assertEquals("param2.2", message.getExpression().getConditions().get(1).getParameters().get(2));

        assertEquals("return true;", message.getScript());
        assertEquals("The error code", message.getErrorCode());
        assertEquals("The error message", message.getErrorMessage());

    }

    @Test
    public void testMarshalling() throws Exception {
        ExpressionEditorMessageJSONMarshaller marshaller = new ExpressionEditorMessageJSONMarshaller();

        ExpressionEditorMessage message = new ExpressionEditorMessage();

        message.setCommand("generateScript");
        ConditionExpression expression = new ConditionExpression("OR");
        message.setExpression(expression);

        Condition condition1 = new Condition();
        condition1.setFunction("isEquals");
        condition1.getParameters().add("variableName1");
        condition1.getParameters().add("param1.1");

        expression.getConditions().add(condition1);


        Condition condition2 = new Condition();
        condition2.setFunction("isBetween");
        condition2.getParameters().add("variableName2");
        condition2.getParameters().add("param2.1");
        condition2.getParameters().add("param2.2");

        expression.getConditions().add(condition2);

        message.setScript("return true;");
        message.setErrorCode("The error code");
        message.setErrorMessage("The error message");

        String expectedResult =
                "{" +
                 "\"command\":\"generateScript\"," +
                 "\"message\":" +
                    "{\"operator\":\"OR\"," +
                    "\"conditions\":[" +
                    "{" +
                    "\"action\":\"isEquals\"," +
                    "\"parameters\":[\"variableName1\",\"param1.1\"]" +
                    "}," +
                    "{" +
                    "\"action\":\"isBetween\"," +
                    "\"parameters\":[\"variableName2\",\"param2.1\",\"param2.2\"]" +
                    "}" +
                    "]," +
                    "\"script\":\"return true;\"," +
                    "\"errorCode\":\"The error code\"," +
                    "\"errorMessage\":\"The error message\"" +
                    "}" +
                "}";

        String result = marshaller.marshall(message);

        assertEquals(expectedResult, result);

    }
}
