
package com.imsweb.validation.entities.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("rule")
public class RuleXmlDto {

    @XStreamAsAttribute
    private String id;

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    @XStreamAlias("java-path")
    private String javaPath;

    @XStreamAsAttribute
    private String condition;

    @XStreamAsAttribute
    private String category;

    @XStreamAsAttribute
    private String depends;

    @XStreamAsAttribute
    private Integer severity;

    @XStreamAsAttribute
    private String agency;

    private String expression;

    private String message;

    private String description;

    @XStreamAlias("history")
    private List<HistoryEventXmlDto> historyEvents;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJavaPath() {
        return javaPath;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDepends() {
        return depends;
    }

    public void setDepends(String depends) {
        this.depends = depends;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<HistoryEventXmlDto> getHistoryEvents() {
        return historyEvents;
    }

    public void setHistoryEvents(List<HistoryEventXmlDto> historyEvents) {
        this.historyEvents = historyEvents;
    }
}
