package com.czbix.v2ex.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.IOException;

public abstract class Parser {
    public static Document toDoc(String html) throws IOException, SAXException {
        return Jsoup.parse(html);
    }

    public static String parseOnceCode(String html) throws IOException, SAXException {
        final Document doc = toDoc(html);
        final Elements ele = doc.select("[name=once]");
        if (ele.size() != 1) {
            throw new ParseException("can't parse once code");
        }

        return ele.get(0).val();
    }

    public static class ParseException extends SAXException {
        public ParseException(String message) {
            super(message);
        }
    }
}
