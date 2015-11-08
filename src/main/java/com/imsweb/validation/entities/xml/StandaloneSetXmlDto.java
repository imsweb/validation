
package com.imsweb.validation.entities.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("set")
public class StandaloneSetXmlDto {

    @XStreamAsAttribute
    private String id;

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private String desc;

    @XStreamImplicit
    private List<StandaloneSetValidatorXmlDto> validators;

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<StandaloneSetValidatorXmlDto> getValidators() {
        return validators;
    }

    public void setValidators(List<StandaloneSetValidatorXmlDto> validators) {
        this.validators = validators;
    }
}
