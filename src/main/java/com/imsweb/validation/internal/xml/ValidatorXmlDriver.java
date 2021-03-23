/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.validation.internal.xml;

import java.io.Writer;

import org.xmlpull.v1.XmlPullParser;

import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.AbstractXppDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

import io.github.xstream.mxparser.MXParser;

public class ValidatorXmlDriver extends AbstractXppDriver {

    /**
     * Construct.
     */
    public ValidatorXmlDriver() {
        super(new XmlFriendlyNameCoder());
    }

    @Override
    protected XmlPullParser createParser() {
        return new MXParser();
    }

    @Override
    public HierarchicalStreamWriter createWriter(Writer out) {
        return new PrettyPrintWriter(out, "    ") {
            boolean _cdata = false;

            @Override
            public void startNode(String name) {
                super.startNode(name);
                _cdata = "entry".equals(name) || "expression".equals(name) || "description".equals(name);
            }

            @Override
            protected void writeText(QuickWriter writer, String text) {
                if (_cdata) {
                    writer.write("<![CDATA[");
                    writer.write(text);
                    writer.write("]]>");
                }
                else
                    super.writeText(writer, text);
            }
        };
    }
}
