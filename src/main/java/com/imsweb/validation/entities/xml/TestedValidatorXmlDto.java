
package com.imsweb.validation.entities.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("tested-validator")
public class TestedValidatorXmlDto {

    @XStreamAsAttribute
    private String id;

    @XStreamImplicit
    private List<TestXmlDto> test;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TestXmlDto> getTest() {
        return test;
    }

    public void setTest(List<TestXmlDto> test) {
        this.test = test;
    }
}
