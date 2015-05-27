package com.czbix.v2ex.parser;

import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.google.common.collect.Lists;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TopicParser {
    public static TopicWithComments parseDoc(Document doc, Topic topic) {
        final Topic.Builder topicBuilder = topic.toBuilder();

        parseContent(topicBuilder, doc.select(".topic_content"));

        // TODO:
        return new TopicWithComments(topicBuilder.createTopic(), Lists.<String>newArrayList());
    }

    private static void parseContent(Topic.Builder builder, Elements elements) {
        if (elements.size() != 1) return;
        final Element ele = elements.get(0);
        builder.setContent(ele.html());
    }
}
