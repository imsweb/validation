
package com.imsweb.validation.entities.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("validator")
public class ValidatorXmlDto {

    @XStreamAsAttribute
    private String id;

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private String version;

    @XStreamAsAttribute()
    @XStreamAlias("min-engine-version")
    private String minEngineVersion;

    @XStreamAsAttribute()
    @XStreamAlias("translated-from")
    private String translatedFrom;

    private List<ReleaseXmlDto> releases;

    @XStreamAlias("deleted-rules")
    private List<DeletedRuleXmlDto> deletedRules;

    @XStreamAlias("context")
    private List<ContextEntryXmlDto> contextEntries;

    private List<CategoryXmlDto> categories;

    private List<ConditionXmlDto> conditions;

    private List<RuleXmlDto> rules;

    private List<SetXmlDto> sets;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMinEngineVersion() {
        return minEngineVersion;
    }

    public void setMinEngineVersion(String minEngineVersion) {
        this.minEngineVersion = minEngineVersion;
    }

    public String getTranslatedFrom() {
        return translatedFrom;
    }

    public void setTranslatedFrom(String translatedFrom) {
        this.translatedFrom = translatedFrom;
    }

    public List<ReleaseXmlDto> getReleases() {
        return releases;
    }

    public void setReleases(List<ReleaseXmlDto> releases) {
        this.releases = releases;
    }

    public List<DeletedRuleXmlDto> getDeletedRules() {
        return deletedRules;
    }

    public void setDeletedRules(List<DeletedRuleXmlDto> deletedRules) {
        this.deletedRules = deletedRules;
    }

    public List<ContextEntryXmlDto> getContextEntries() {
        return contextEntries;
    }

    public void setContextEntries(List<ContextEntryXmlDto> contextEntries) {
        this.contextEntries = contextEntries;
    }

    public List<CategoryXmlDto> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryXmlDto> categories) {
        this.categories = categories;
    }

    public List<ConditionXmlDto> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionXmlDto> conditions) {
        this.conditions = conditions;
    }

    public List<RuleXmlDto> getRules() {
        return rules;
    }

    public void setRules(List<RuleXmlDto> rules) {
        this.rules = rules;
    }

    public List<SetXmlDto> getSets() {
        return sets;
    }

    public void setSets(List<SetXmlDto> sets) {
        this.sets = sets;
    }
}
