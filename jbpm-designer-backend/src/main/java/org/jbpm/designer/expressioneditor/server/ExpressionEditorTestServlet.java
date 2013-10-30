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
import org.jbpm.designer.expressioneditor.model.ExpressionEditorMessage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ExpressionEditorTestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doProcess(req, res);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doProcess(req, res);
    }

    private void doProcess(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setStatus(200);

        try {

            ExpressionEditorMessageJSONMarshaller marshaller = new ExpressionEditorMessageJSONMarshaller();
            ExpressionEditorMessageJSONUnmarshaller unmarshaller = new ExpressionEditorMessageJSONUnmarshaller();
            ExpressionEditorMessage expressionEditorMessage = null;

            PrintWriter out = res.getWriter();

            String command = req.getParameter("expressionEditorCommand");

            if (command != null) {
                expressionEditorMessage = unmarshaller.unmarshall(command);

                if ("generateScript".equals(expressionEditorMessage.getCommand())) {
                    expressionEditorMessage.setScript("return true; //generated at: "+new java.util.Date());
                } else if ("parseScript".equals(expressionEditorMessage.getCommand())) {
                    expressionEditorMessage.setScript("return true; //parsed at: "+new java.util.Date());
                }

                if (expressionEditorMessage == null) {
                    expressionEditorMessage = new ExpressionEditorMessage();
                    expressionEditorMessage.setErrorMessage("invalid message");
                }

                String json  = marshaller.marshall(expressionEditorMessage);

                out.write(json);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
