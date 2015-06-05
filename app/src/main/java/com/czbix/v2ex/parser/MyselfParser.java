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

    public static Avatar parseAvatarOnly(Document doc) throws IOException, SAXException {
        final Elements elements = doc.select("#Rightbar > div:nth-child(2)");
        Preconditions.checkState(elements.size() == 1);

        Element ele = elements.get(0);
        final String url = ele.select(".avatar").get(0).attr("src");
        return new Avatar.Builder().setUrl(url).createAvatar();
    }

    /**
     * @return null if user signed out
     */
    public static MySelfInfo parseDoc(Document doc) {
        final Elements elements = doc.select("#Rightbar > div:nth-child(2)");
        Preconditions.checkState(elements.size() == 1);

        Element ele = elements.get(0);
        final Elements children = ele.children();
        if (children.size() <= 2) {
            // user signed out
            return null;
        }

        final String text = ele.select(".inner > strong > a").get(0).text();
        final Matcher matcher = PATTERN_UNREAD_NUM.matcher(text);
        Preconditions.checkState(matcher.find());
        final int num = Integer.parseInt(matcher.group());

        return new MySelfInfo(num);
    }

    public static class MySelfInfo {
        public final int mUnread;

        public MySelfInfo(int unread) {
            mUnread = unread;
        }
    }
}
