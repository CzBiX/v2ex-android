package com.czbix.v2ex.parser;

import com.czbix.v2ex.BuildConfig;
import com.google.common.base.Preconditions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public abstract class Parser {
    public static Document toDoc(String html) {
        final Document document = Jsoup.parse(html);
        if (!BuildConfig.DEBUG) {
            final Document.OutputSettings settings = document.outputSettings().prettyPrint(false);
            document.outputSettings(settings);
        }
        return document;
    }

    public static String parseOnceCode(String html) {
        final Document doc = toDoc(html);
        final Elements elements = doc.select("[name=once]");
        Preconditions.checkState(elements.size() == 1, "once code size isn't one");

        return elements.get(0).val();
    }
}
