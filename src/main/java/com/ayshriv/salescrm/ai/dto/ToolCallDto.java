package com.ayshriv.salescrm.ai.dto;

import java.io.Serializable;

public class ToolCallDto implements Serializable {

    private String toolName;
    private String arguments;
    private String result;

    public ToolCallDto() {
    }

    public ToolCallDto(String toolName, String arguments, String result) {
        this.toolName = toolName;
        this.arguments = arguments;
        this.result = result;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
