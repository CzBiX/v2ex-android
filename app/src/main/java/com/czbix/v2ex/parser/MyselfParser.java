package com.czbix.v2ex.parser;

import com.czbix.v2ex.helper.JsoupObjects;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.LoginResult;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyselfParser extends Parser {
    private static final Pattern PATTERN_UNREAD_NUM = Pattern.compile("\\d+");

    public static LoginResult parseLoginResult(Document doc) {
        Element tr = new JsoupObjects(doc).body().child("#Wrapper").child(".content")
                .child("#Rightbar").dfs("tr").getOne();

        final String url = new JsoupObjects(tr).dfs(".avatar").getOne().attr("src");
        final Avatar avatar = new Avatar.Builder().setUrl(url).createAvatar();

        final String username = new JsoupObjects(tr).child("td").child(".bigger").child("a")
                .getOne().text();
        return new LoginResult(username, avatar);
    }

    /**
     * @return null if user signed out
     */
    public static MySelfInfo parseDoc(Document doc, boolean isTab) {
        Element box = new JsoupObjects(doc).body().child("#Wrapper").child(".content")
                .child("#Rightbar").child(".box").getOne();

        final Elements children = box.children();
        if (children.size() <= 2) {
            // user signed out
            return null;
        }

        final int num = getNotificationsNum(box);
        final boolean hasAward = isTab && hasAwardInTab(box);

        return new MySelfInfo(num, hasAward);
    }

    private static boolean hasAwardInTab(Element box) {
        Optional<Element> optional = new JsoupObjects(box.parent()).child(".box")
                .child(".inner").child(".fa-gift").getOptional();
        return optional.isPresent();
    }

    public static boolean hasAward(String html) {
        final Document doc = toDoc(html);
        Optional<Element> optional = new JsoupObjects(doc).body().child("#Wrapper")
                .child(".content").child("#Main").child(".box").child(".cell").child(".gray")
                .child(".fa-ok-sign").getOptional();
        return !optional.isPresent();
    }

    static int getNotificationsNum(Element ele) {
        final String text = new JsoupObjects(ele).child(".inner").child(".fade").getOne().text();
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
