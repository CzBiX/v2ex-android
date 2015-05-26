package com.czbix.v2ex.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

public abstract class Parser {
    public static Document toDoc(String html) throws IOException, SAXException {
        return Jsoup.parse(html);
    }
}
