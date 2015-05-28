package com.czbix.v2ex.parser;

import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.google.common.collect.Lists;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class TopicParser {
    public static TopicWithComments parseDoc(Document doc, Topic topic) {
        final Topic.Builder topicBuilder = topic.toBuilder();

        parseContent(topicBuilder, doc.select(".topic_content"));
        List<Comment> comments = parseComments(doc.select("#Main > div:nth-child(4) tr"));

        // TODO:
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
        final String content = ele.select(".reply_content").html();
        return new Comment.Builder().setContent(content).createComment();
    }

    private static void parseContent(Topic.Builder builder, Elements elements) {
        if (elements.size() != 1) return;
        final Element ele = elements.get(0);
        builder.setContent(ele.html());
    }
}
