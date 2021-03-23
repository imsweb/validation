/*
 * Copyright (C) 2021 Information Management Services, Inc.
 */
package com.imsweb.validation.internal.xml;

import java.io.Writer;

import org.xmlpull.v1.XmlPullParser;

import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.AbstractXppDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

import io.github.xstream.mxparser.MXParser;

public class StandaloneSetXmlDriver extends AbstractXppDriver {

    /**
     * Construct.
     */
    public StandaloneSetXmlDriver() {
        super(new XmlFriendlyNameCoder());
    }

    @Override
    protected XmlPullParser createParser() {
        return new MXParser();
    }

    @Override
    public HierarchicalStreamWriter createWriter(Writer out) {
        return new PrettyPrintWriter(out, "    ");
    }
}
