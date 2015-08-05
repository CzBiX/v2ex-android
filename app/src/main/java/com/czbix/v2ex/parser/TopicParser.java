package com.czbix.v2ex.parser;

import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.model.Postscript;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopicParser extends Parser {
    private static final Pattern PATTERN_CSRF_TOKEN = Pattern.compile("var csrfToken = \"(\\w{32})\"");
    private static final Pattern PATTERN_TOPIC_REPLY_TIME = Pattern.compile("·\\s*(.+?)(?:\\s+·|$)");
    private static final Pattern PATTERN_POSTSCRIPT = Pattern.compile("·\\s+(.+)");
    private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\d+");

    public static TopicWithComments parseDoc(Document doc, Topic topic) {
        final Topic.Builder topicBuilder = topic.toBuilder();

        parseTopicContent(topicBuilder, doc.select(".cell .topic_content"));
        final List<Postscript> postscripts = parsePostscript(doc.select(".subtle"));
        parseTopicInfo(topicBuilder, doc);
        List<Comment> comments = parseComments(doc.select("#Main > div:nth-child(4) tr"));
        int[] pageNum = getMaxPage(doc);

        final String csrfToken;
        final String onceToken;
        if (UserState.getInstance().isLoggedIn()) {
            csrfToken = parseCsrfToken(doc);
            onceToken = parseOnceToken(doc);
            topicBuilder.isFavorited(parseFavorited(doc));
        } else {
            csrfToken = null;
            onceToken = null;
        }
        return new TopicWithComments(topicBuilder.createTopic(), comments, postscripts, pageNum[0],
                pageNum[1], csrfToken, onceToken);
    }

    private static int[] getMaxPage(Document doc) {
        final Elements elements = doc.select("#Main > div:nth-child(4) > .inner");
        if (elements.size() <= 1) {
            return new int[]{1, 1};
        }

        final int maxPage = elements.get(1).children().size();
        final int curPage = elements.select(".page_current").get(0).elementSiblingIndex() + 1;

        return new int[]{curPage, maxPage};
    }

    private static String parseCsrfToken(Document doc) {
        final String html = doc.head().html();
        final Matcher matcher = PATTERN_CSRF_TOKEN.matcher(html);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }

    private static String parseOnceToken(Document doc) {
        final Elements elements = doc.select("form > input[name=once]");
        if (elements.size() != 1) {
            return null;
        }

        return elements.get(0).val();
    }

    private static boolean parseFavorited(Document doc) {
        final Elements elements = doc.select(".topic_buttons .tb:nth-child(2)");
        Preconditions.checkState(elements.size() >= 1, "should has a tag for favorite link");
        final Element ele = elements.get(0);
        Preconditions.checkState(ele.tagName().equals("a"), "should be a tag for favorite link");

        return ele.attr("href").startsWith("/unfav");
    }

    private static void parseTopicInfo(Topic.Builder builder, Document doc) {
        parseTopicReplyCount(builder, doc);

        final Element headerEle = doc.select("#Main .header").get(0);
        parseTopicReplyTime(builder, headerEle.select(".gray").get(0).textNodes().get(0));
        builder.setNode(TopicListParser.parseNode(headerEle.select(".chevron").get(0).nextElementSibling()));
        parseTopicTitle(builder, headerEle);

        if (!builder.hasInfo()) {
            TopicListParser.parseMember(builder, headerEle.child(0));
        }
    }

    static void parseTopicReplyTime(Topic.Builder topicBuilder, TextNode textNode) {
        final String text = textNode.text();
        final Matcher matcher = PATTERN_TOPIC_REPLY_TIME.matcher(text);
        if (!matcher.find()) {
            throw new FatalException("match reply time for topic failed: " + text);
        }
        final String time = matcher.group(1);
        topicBuilder.setReplyTime(time);
    }

    private static void parseTopicTitle(Topic.Builder builder, Element headerEle) {
        final String title = headerEle.select("h1").html();

        builder.setTitle(title);
    }

    private static void parseTopicReplyCount(Topic.Builder topicBuilder, Document doc) {
        final Elements elements = doc.select("#Main > div:nth-child(4) > .cell:nth-child(1) > span");
        if (elements.size() == 0) {
            // empty reply
            topicBuilder.setReplyCount(0);
            return;
        }
        final String text = elements.get(0).ownText();
        final Matcher matcher = PATTERN_NUMBERS.matcher(text);
        Preconditions.checkState(matcher.find());

        topicBuilder.setReplyCount(Integer.parseInt(matcher.group()));
    }

    private static List<Comment> parseComments(Elements elements) {
        List<Comment> list = Lists.newArrayListWithCapacity(elements.size());

        for (Element ele : elements) {
            list.add(parseComment(ele));
        }

        return list;
    }

    private static Comment parseComment(Element ele) {

        final Avatar.Builder avatarBuilder = new Avatar.Builder();
        parseAvatar(avatarBuilder, ele);

        final Member.Builder memberBuilder = new Member.Builder();
        memberBuilder.setAvatar(avatarBuilder.createAvatar());
        parseMember(memberBuilder, ele);

        final Comment.Builder commentBuilder = new Comment.Builder();
        commentBuilder.setMember(memberBuilder.createMember());
        parseInfo(commentBuilder, ele);
        parseContent(commentBuilder, ele);

        return commentBuilder.createComment();
    }

    private static void parseContent(Comment.Builder builder, Element ele) {
        ele = ele.select(".reply_content").get(0);
        builder.setContent(ele.html());
    }

    private static void parseInfo(Comment.Builder builder, Element ele) {
        final Element tableEle = ele.parent().parent().parent();
        // example data: r_123456
        final int id = Integer.parseInt(tableEle.id().substring(2));
        builder.setId(id);

        final boolean thanked = ele.select(".thank_area").hasClass("thanked");
        builder.setThanked(thanked);

        final String text = ele.select(".no").text();
        builder.setFloor(Integer.parseInt(text));

        final Elements elements = ele.select(".small");

        final Element timeEle = elements.get(0);
        builder.setReplyTime(timeEle.text());

        if (elements.size() == 2) {
            final Matcher matcher = PATTERN_NUMBERS.matcher(elements.get(1).text());
            Preconditions.checkState(matcher.find());

            final int thanks = Integer.parseInt(matcher.group());
            builder.setThanks(thanks);
        }
    }

    private static void parseMember(Member.Builder builder, Element ele) {
        ele = ele.select(".dark").get(0);

        builder.setUsername(ele.text());
    }

    private static void parseAvatar(Avatar.Builder builder, Element ele) {
        ele = ele.select(".avatar").get(0);

        builder.setUrl(ele.attr("src"));
    }

    private static void parseTopicContent(Topic.Builder builder, Elements elements) {
        if (elements.size() == 0) return;
        Element ele = elements.get(0);
        builder.setContent(ele.html());
    }

    private static List<Postscript> parsePostscript(Elements elements) {
        List<Postscript> list = Lists.newArrayListWithCapacity(elements.size());

        for (Element element : elements) {
            Element ele = element.select(".fade").get(0);
            final Matcher matcher = PATTERN_POSTSCRIPT.matcher(ele.text());
            Preconditions.checkArgument(matcher.find());

            final String time = matcher.group(1);
            ele = element.select(".topic_content").get(0);
            final String content = ele.html();

            list.add(new Postscript(content, time));
        }

        return list;
    }

    public static String parseProblemInfo(String html) {
        final Document doc = Parser.toDoc(html);
        final Elements elements = doc.select(".problem ul:first-child");
        Preconditions.checkState(elements.size() == 1, "problem size isn't one");

        return elements.get(0).html();
    }
}
