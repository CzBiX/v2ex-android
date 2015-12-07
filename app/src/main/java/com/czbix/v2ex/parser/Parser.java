package com.czbix.v2ex.parser;

import com.czbix.v2ex.BuildConfig;
import com.czbix.v2ex.helper.JsoupObjects;
import com.google.common.base.Preconditions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
        Element ele = new JsoupObjects(doc).body().child("#Wrapper").child(".content")
                .child("#Main").child(".box").child(".cell").dfs("form").dfs("[name=once]").getOne();

        return ele.val();
    }
}
