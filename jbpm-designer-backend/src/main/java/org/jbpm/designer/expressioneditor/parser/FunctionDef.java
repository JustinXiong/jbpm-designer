package org.jbpm.designer.expressioneditor.parser;

import java.util.ArrayList;
import java.util.List;

public class FunctionDef {

    private String name;

    private List<ParamDef> params = new ArrayList<ParamDef>();

    public FunctionDef(String name) {
        this.name = name;
    }

    public FunctionDef(String name, List<ParamDef> params) {
        this.name = name;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ParamDef> getParams() {
        return params;
    }

    public void setParams(List<ParamDef> params) {
        this.params = params;
    }

    public void addParam(String name, Class type) {
        params.add(new ParamDef(name, type));
    }
}
