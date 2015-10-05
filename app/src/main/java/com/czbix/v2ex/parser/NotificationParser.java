package com.czbix.v2ex.parser;

import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.model.Notification;
import com.czbix.v2ex.model.Notification.NotificationType;
import com.czbix.v2ex.model.Topic;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationParser {
    private static final Pattern PATTERN_TOKEN = Pattern.compile("http://www.v2ex.com/n/(.+).xml");

    public static List<Notification> parseDoc(Document doc) {
        Elements elements = doc.select("#Main > div:nth-child(2) .cell tr");
        List<Notification> result = Lists.newArrayListWithCapacity(elements.size());

        for (Element element : elements) {
            result.add(parseNotification(element));
        }

        return result;
    }

    public static int parseUnreadCount(Document doc) {
        final Elements elements = doc.select("#Rightbar > div:nth-child(2)");
        Preconditions.checkState(elements.size() == 1);

        return MyselfParser.getNotificationsNum(elements.get(0));
    }

    private static Notification parseNotification(Element element) {
        Notification.Builder builder = new Notification.Builder();
        Member member = parseMember(element.child(0));
        builder.setMember(member);

        Element ele = element.child(1);
        parseInfo(builder, ele);
        parseContent(builder, ele);

        return builder.createNotification();
    }

    private static void parseContent(Notification.Builder builder, Element ele) {
        Elements elements = ele.select(".payload");
        if (elements.size() != 1) {
            // don't have content
            return;
        }

        builder.setContent(elements.get(0).html());
    }

    private static void parseInfo(Notification.Builder builder, Element ele) {
        builder.setTime(parseTime(ele));

        Element fadeEle = ele.child(0);
        builder.setType(parseAction(fadeEle));
        builder.setTopic(parseTopic(fadeEle));
    }

    private static Topic parseTopic(Element ele) {
        ele = ele.child(1);
        String url = ele.attr("href");

        int id = Topic.getIdFromUrl(url);
        String title = ele.text();

        return new Topic.Builder().setId(id).setTitle(title).createTopic();
    }

    @NotificationType
    private static int parseAction(Element ele) {
        String text = ele.textNodes().get(0).text();

        if (text.contains("在回复")) {
            return Notification.TYPE_REPLY_COMMENT;
        } else if (text.contains("感谢了你在主题")) {
            return Notification.TYPE_THANK_COMMENT;
        } else if (text.contains("收藏了你发布的主题")) {
            return Notification.TYPE_FAV_TOPIC;
        } else if (text.contains("感谢了你发布的主题 ")) {
            return Notification.TYPE_THANK_TOPIC;
        } else if (text.contains("在")) {
            return Notification.TYPE_REPLY_TOPIC;
        }

        return Notification.TYPE_UNKNOWN;
    }

    private static String parseTime(Element ele) {
        Element timeEle = ele.select(".snow").get(0);
        return timeEle.text();
    }

    private static Member parseMember(Element ele) {
        final Member.Builder memberBuilder = new Member.Builder();

        // get member url
        ele = ele.child(0);
        Preconditions.checkState(ele.tagName().equals("a"));
        final String url = ele.attr("href");
        memberBuilder.setUsername(Member.getNameFromUrl(url));

        // get member avatar
        final Avatar.Builder avatarBuilder = new Avatar.Builder();
        ele = ele.child(0);
        Preconditions.checkState(ele.tagName().equals("img"));
        avatarBuilder.setUrl(ele.attr("src"));
        memberBuilder.setAvatar(avatarBuilder.createAvatar());

        return memberBuilder.createMember();
    }

    public static String parseToken(String html) {
        final Document doc = Parser.toDoc(html);
        final Elements elements = doc.select(".sll");
        Preconditions.checkState(elements.size() == 1, "token elements size should be one");

        final Element ele = elements.get(0);
        final String val = ele.val();

        final Matcher matcher = PATTERN_TOKEN.matcher(val);
        Preconditions.checkState(matcher.matches(), "val not match token pattern");

        return matcher.group(1);
    }
}
