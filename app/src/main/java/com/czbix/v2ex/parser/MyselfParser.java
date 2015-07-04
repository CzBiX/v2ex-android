package com.czbix.v2ex.parser;

import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.LoginResult;
import com.google.common.base.Preconditions;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyselfParser extends Parser {
    private static final Pattern PATTERN_UNREAD_NUM = Pattern.compile("\\d+");

    public static LoginResult parseLoginResult(Document doc) {
        final Elements elements = doc.select("#Rightbar > div:nth-child(2)");
        Preconditions.checkState(elements.size() == 1, "sidebar size not match");

        Element ele = elements.get(0);
        final String url = ele.select(".avatar").get(0).attr("src");
        final Avatar avatar = new Avatar.Builder().setUrl(url).createAvatar();

        final String username = ele.select(".bigger a").get(0).text();
        return new LoginResult(username, avatar);
    }

    /**
     * @return null if user signed out
     */
    public static MySelfInfo parseDoc(Document doc, boolean isTab) {
        final Elements elements = doc.select("#Rightbar > div:nth-child(2)");
        Preconditions.checkState(elements.size() == 1);

        Element ele = elements.get(0);
        final Elements children = ele.children();
        if (children.size() <= 2) {
            // user signed out
            return null;
        }

        final int num = getNotificationsNum(ele);
        final boolean hasAward = isTab && hasAwardInTab(doc);

        return new MySelfInfo(num, hasAward);
    }

    public static boolean hasAwardInTab(Document doc) {
        final Elements elements = doc.select("#Rightbar .fa-gift");
        return elements.size() == 1;
    }

    public static boolean hasAward(String html) {
        final Document doc = toDoc(html);
        final Elements elements = doc.select(".fa-ok-sign");
        return elements.size() == 0;
    }

    static int getNotificationsNum(Element ele) {
        final String text = ele.select(".inner a[href=/notifications]").get(0).text();
        final Matcher matcher = PATTERN_UNREAD_NUM.matcher(text);
        Preconditions.checkState(matcher.find());
        return Integer.parseInt(matcher.group());
    }

    public static class MySelfInfo {
        public final int mUnread;
        public final boolean mHasAward;

        public MySelfInfo(int unread, boolean hasAward) {
            mUnread = unread;
            mHasAward = hasAward;
        }
    }
}
