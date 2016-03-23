package com.czbix.v2ex.parser;

import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.helper.JsoupObjects;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Page;
import com.czbix.v2ex.model.Tab;
import com.czbix.v2ex.model.Topic;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopicListParser extends Parser {
    private static final Pattern PATTERN_REPLY_TIME = Pattern.compile("•\\s*(.+?)(?:\\s+•|$)");

    public static List<Topic> parseDoc(Document doc, Page page) {
        final Element contentBox = new JsoupObjects(doc).bfs("body").child("#Wrapper")
                .child(".content").child("#Main").child(".box").getOne();

        if (page instanceof Node) {
            return parseDocForNode(contentBox, (Node) page);
        } else if (page instanceof Tab || Page.PAGE_FAV_TOPIC.equals(page)) {
            return parseDocForTab(contentBox);
        } else {
            throw new IllegalArgumentException("unknown page type: " + page);
        }
    }

    private static List<Topic> parseDocForTab(Element contentBox) {
        final JsoupObjects elements = new JsoupObjects(contentBox).child(".item")
                .child("table").child("tbody").child("tr");
        return Lists.newArrayList(Iterables.transform(elements, TopicListParser::parseItemForTab));
    }

    private static List<Topic> parseDocForNode(Element contentBox, Node node) {
        final JsoupObjects elements = new JsoupObjects(contentBox).child("#TopicsNode")
                .child(".cell").child("table").child("tbody").child("tr");
        return Lists.newArrayList(Iterables.transform(elements,
                e -> parseItemForNode(e, node)));
    }

    private static Topic parseItemForTab(Element item) {
        final Elements list = item.children();

        final Topic.Builder topicBuilder = new Topic.Builder();
        parseMember(topicBuilder, list.get(0));

        final Element ele = list.get(2);
        parseTitle(topicBuilder, ele);
        parseInfo(topicBuilder, ele, null);

        parseReplyCount(topicBuilder, list.get(3));

        return topicBuilder.createTopic();
    }

    private static Topic parseItemForNode(Element item, Node node) {
        final Elements list = item.children();

        final Topic.Builder topicBuilder = new Topic.Builder();
        parseMember(topicBuilder, list.get(0));

        final Element ele = list.get(2);
        parseTitle(topicBuilder, ele);
        parseInfo(topicBuilder, ele, node);

        parseReplyCount(topicBuilder, list.get(3));

        return topicBuilder.createTopic();
    }

    private static void parseReplyCount(Topic.Builder topicBuilder, Element ele) {
        final Elements children = ele.children();
        final int count;
        if (children.size() > 0) {
            final String numStr = ele.child(0).text();
            count = Integer.parseInt(numStr);
        } else {
            // do not have reply yet
            count = 0;
        }
        topicBuilder.setReplyCount(count);
    }

    private static void parseInfo(Topic.Builder topicBuilder, Element ele, Node node) {
        ele = JsoupObjects.child(ele, ".fade");

        boolean hasNode;
        if (node == null) {
            hasNode = false;
            node = parseNode(JsoupObjects.child(ele, ".node"));
        } else {
            hasNode = true;
        }
        topicBuilder.setNode(node);

        final int index = hasNode ? 0 : 1;
        if (ele.textNodes().size() > index) {
            parseReplyTime(topicBuilder, ele.textNodes().get(index));
        } else {
            // reply time may not exists
            topicBuilder.setReplyTime("");
        }
    }

    private static void parseReplyTime(Topic.Builder topicBuilder, TextNode textNode) {
        final String text = textNode.text();
        final Matcher matcher = PATTERN_REPLY_TIME.matcher(text);
        if (!matcher.find()) {
            throw new FatalException("match reply time for topic failed: " + text);
        }
        final String time = matcher.group(1);
        topicBuilder.setReplyTime(time);
    }

    private static void parseTitle(Topic.Builder topicBuilder, Element ele) {
        ele = new JsoupObjects(ele).child(".item_title").child("a").getOne();
        String url = ele.attr("href");

        topicBuilder.setId(Topic.getIdFromUrl(url));
        topicBuilder.setTitle(ele.html());
    }

    static void parseMember(Topic.Builder builder, Element ele) {
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

        builder.setMember(memberBuilder.createMember());
    }
}
