package com.czbix.v2ex.parser;

import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Page;
import com.czbix.v2ex.model.Tab;
import com.czbix.v2ex.model.Topic;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopicListParser extends Parser {
    private static final Pattern PATTERN_REPLY_TIME = Pattern.compile("•\\s*(.+?)(?:\\s+•|$)");

    public static List<Topic> parseDoc(Document doc, Page page) throws IOException, SAXException {
        if (page instanceof Node) {
            return parseDocForNode(doc, (Node) page);
        } else if (page instanceof Tab) {
            return parseDocForTab(doc);
        } else {
            throw new IllegalArgumentException("unknown page type: " + page);
        }
    }

    private static List<Topic> parseDocForTab(Document doc) throws IOException, SAXException {
        final Elements elements = doc.select("#Main > div:nth-child(2) > .item  tr");
        final List<Topic> result = Lists.newArrayListWithCapacity(elements.size());
        for (Element item : elements) {
            result.add(parseItemForTab(item));
        }
        return result;
    }

    private static List<Topic> parseDocForNode(Document doc, Node node) throws IOException, SAXException {
        final Elements elements = doc.select("#TopicsNode > .cell  tr");
        final List<Topic> result = Lists.newArrayListWithCapacity(elements.size());
        for (Element item : elements) {
            result.add(parseItemForNode(item, node));
        }

        return result;
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
        ele = ele.select(".small").get(0);

        boolean hasNode;
        if (node == null) {
            hasNode = false;
            final Elements nodeEle = ele.select("> a");
            final String title = nodeEle.text();
            final String url = nodeEle.attr("href");
            final String name = Node.getNameFromUrl(url);

            node = new Node.Builder().setTitle(title).setName(name).createNode();
        } else {
            hasNode = true;
        }
        topicBuilder.setNode(node);

        final String text = ele.textNodes().get(hasNode ? 0 : 1).text();
        final Matcher matcher = PATTERN_REPLY_TIME.matcher(text);
        if (!matcher.find()) {
            throw new FatalException("match reply time for topic failed: " + text);
        }
        final String time = matcher.group(1);
        topicBuilder.setReplyTime(time);
    }

    private static void parseTitle(Topic.Builder topicBuilder, Element ele) {
        ele = ele.select(".item_title > a").get(0);
        Preconditions.checkState(ele.tagName().equals("a"));
        String url = ele.attr("href");

        topicBuilder.setId(Topic.getIdFromUrl(url));
        topicBuilder.setTitle(ele.text());
    }

    private static void parseMember(Topic.Builder builder, Element ele) {
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
