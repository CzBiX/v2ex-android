package com.czbix.v2ex.parser;

import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.google.common.collect.Lists;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class TopicParser extends Parser {
    public static TopicWithComments parseDoc(Document doc, Topic topic) {
        final Topic.Builder topicBuilder = topic.toBuilder();

        parseContent(topicBuilder, doc.select(".topic_content"));
        List<Comment> comments = parseComments(doc.select("#Main > div:nth-child(4) tr"));

        return new TopicWithComments(topicBuilder.createTopic(), comments);
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

        final Element timeEle = ele.select(".small").get(0);

        builder.setReplyTime(timeEle.text());
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
        if (elements.size() != 1) return;
        final Element ele = elements.get(0);
        builder.setContent(ele.html());
    }
}
