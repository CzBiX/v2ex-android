package com.czbix.v2ex.parser;

import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopicParser extends Parser {
    private static final Pattern PATTERN_CSRF_TOKEN = Pattern.compile("var csrfToken = \"(\\w{32})\"");
    private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\d+");

    public static TopicWithComments parseDoc(Document doc, Topic topic) {
        final Topic.Builder topicBuilder = topic.toBuilder();

        parseContent(topicBuilder, doc.select(".topic_content"));
        parseTopicInfo(topicBuilder, doc);
        List<Comment> comments = parseComments(doc.select("#Main > div:nth-child(4) tr"));

        final String csrfToken = parseCsrfToken(doc);
        final String onceToken = parseOnceToken(doc);
        return new TopicWithComments(topicBuilder.createTopic(), comments, csrfToken, onceToken);
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

    private static void parseTopicInfo(Topic.Builder topicBuilder, Document doc) {
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

    private static void parseContent(Topic.Builder builder, Elements elements) {
        if (elements.size() == 0) return;
        Element ele = elements.get(0);
        if (ele.children().size() == 1 && ele.child(0).hasClass("markdown_body")) {
            // remove markdown body div
            ele = ele.child(0);
        }
        builder.setContent(ele.html());
    }
}
