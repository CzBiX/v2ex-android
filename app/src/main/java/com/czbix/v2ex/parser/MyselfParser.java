package com.czbix.v2ex.parser;

import com.czbix.v2ex.model.Avatar;
import com.google.common.base.Preconditions;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyselfParser extends Parser {
    private static final Pattern PATTERN_UNREAD_NUM = Pattern.compile("\\d+");

    public static MySelfInfo parseDoc(Document doc) throws IOException, SAXException {
        final Elements elements = doc.select("#Rightbar > div:nth-child(2)");
        Preconditions.checkState(elements.size() == 1);

        Element ele = elements.get(0);
        final String url = ele.select(".avatar").get(0).attr("src");
        final Avatar avatar = new Avatar.Builder().setUrl(url).createAvatar();

        final String text = ele.select(".inner .fade").get(0).text();
        final Matcher matcher = PATTERN_UNREAD_NUM.matcher(text);
        Preconditions.checkState(matcher.find());
        final int num = Integer.parseInt(matcher.group());

        return new MySelfInfo(avatar, num);
    }

    public static class MySelfInfo {
        public final Avatar mAvatar;
        public final int mUnread;

        public MySelfInfo(Avatar avatar, int unread) {
            mAvatar = avatar;
            mUnread = unread;
        }
    }
}
