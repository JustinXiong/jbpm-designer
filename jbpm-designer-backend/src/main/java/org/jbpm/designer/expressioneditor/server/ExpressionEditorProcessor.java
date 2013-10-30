/**
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.designer.expressioneditor.server;

import org.jbpm.designer.expressioneditor.marshalling.ExpressionEditorMessageJSONMarshaller;
import org.jbpm.designer.expressioneditor.marshalling.ExpressionEditorMessageJSONUnmarshaller;
import org.jbpm.designer.expressioneditor.model.Condition;
import org.jbpm.designer.expressioneditor.model.ConditionExpression;
import org.jbpm.designer.expressioneditor.model.ExpressionEditorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ExpressionEditorProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionEditorProcessor.class);

    private static final String PARSE_COMMAND = "parseScript";

    private static final String GENERATE_COMMAND = "generateScript";

    public ExpressionEditorProcessor() {
    }

    private void doProcess(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");

        try {

            ExpressionEditorMessageJSONMarshaller marshaller = new ExpressionEditorMessageJSONMarshaller();
            ExpressionEditorMessageJSONUnmarshaller unmarshaller = new ExpressionEditorMessageJSONUnmarshaller();
            ExpressionEditorMessage requestMessage = null;
            ExpressionEditorMessage responseMessage = null;

            PrintWriter out = res.getWriter();

            String command = req.getParameter("command");
            String message = req.getParameter("message");

            if (logger.isDebugEnabled()) {
                logger.debug("processing request for request parameters, command: " + command + ", message: " + message);
            }

            if (!isValidCommand(command)) {
                logger.error("Invalid command: " + command + " was sent to the ExpressionsEditorProcessor, " +
                        "request will be discarded.");
                return;
            }

            try {
                requestMessage = unmarshaller.unmarshall(message);
            } catch (Exception e) {
                logger.error("It was not possible to unmarshall message: " + message, e);
                logger.error("Request will be discarded.");
                return;
            }

            if (GENERATE_COMMAND.equals(command)) {
                responseMessage = doGenerateScript(requestMessage);
            } else if (PARSE_COMMAND.equals(command)) {
                responseMessage = doParseScript(requestMessage);
            }

            if (responseMessage != null) {
                try {

                    String jsonResponse = marshaller.marshall(responseMessage);
                    if (logger.isDebugEnabled()) {
                        logger.debug("sending response message: " + jsonResponse);
                    }
                    out.write(jsonResponse);
                } catch (Exception e) {
                    //unexpected error.
                    logger.error("It was not possible to marshal the responseMessage: " + responseMessage, e);
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error during request processing.", e);
        }
    }

    private ExpressionEditorMessage doParseScript(ExpressionEditorMessage requestMessage) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private ExpressionEditorMessage doGenerateScript(ExpressionEditorMessage requestMessage) {
        ExpressionEditorMessage responseMessage = new ExpressionEditorMessage();
        List<String> errors = new ArrayList<String>();

        if (isValidMessageForCommand(GENERATE_COMMAND, requestMessage)) {
            ConditionExpression expression = requestMessage.getExpression();
            String operator = null;
            StringBuilder script = new StringBuilder();
            int validTerms = 0;

            //First version implementation. At the moment we don't need a more elaborated programming.
            //TODO we can provide a more elaborated generation if needed.
            if ("OR".equals(expression.getOperator())) {
                operator = "||";
            } else if ("AND".equals(expression.getOperator())) {
                operator = "&&";
            } else if (expression.getConditions().size() > 1) {
                //we have multiple conditions and the operator is not defined.
                //the default operator will be AND
                operator = "&&";
            }

            for (Condition condition : expression.getConditions()) {
                if (addConditionToScript(condition, script, operator, validTerms, errors) > 0) {
                    validTerms++;
                } else {
                    //we have an invalid condition.
                    //at the moment the approach is that all the generation fails.
                    requestMessage.setErrorCode(ExpressionEditorErrors.INVALID_CONDITION_ERROR);
                    return requestMessage;
                }
            }

            responseMessage.setScript("return " + script.toString() + ";");

        } else {
            responseMessage.setErrorCode(ExpressionEditorErrors.INVALID_MESSAGE_ERROR);
        }
        return responseMessage;
    }

    private int addConditionToScript(final Condition condition, final StringBuilder script, final String operator, final int validTerms, final List<String> errors) {
        if (condition == null) return 0;
        if (isValidFunction(condition.getFunction())) {
            errors.add("Invalid function : " + condition.getFunction());
            return 0;
        }
        //TODO evaluate if we put more validations.
        if (validTerms > 0) {
            script.append(" " + operator + " ");
        } else {
            script.append(" ");
        }
        script.append(condition.getFunction().trim());
        script.append("(");
        boolean first = true;
        for (String param : condition.getParameters()) {
            if (first) {
                //first parameter is always a process variable name.
                script.append(param);
                first = false;
            } else {
                //the other parameters are always string parameters.
                script.append(", ");
                script.append("\""+param+"\"");
            }
        }
        script.append(")");
        return 1;
    }

    boolean isValidFunction(String function) {
        return function != null && !"".equals(function.trim());
    }

    boolean isValidMessageForCommand(String command, ExpressionEditorMessage message) {
        if (GENERATE_COMMAND.equals(command)) {
            if (message.getExpression() == null) {
                logger.error("No expression is present in message: " + message);
                return false;
            }
        }

        return true;
    }

    boolean isValidCommand(String command) {
        return PARSE_COMMAND.equals(command) || GENERATE_COMMAND.equals(command);
    }
}
