package com.czbix.v2ex.parser;

import com.czbix.v2ex.common.UserState;
import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.helper.JsoupObjects;
import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Comment;
import com.czbix.v2ex.model.Member;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Postscript;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.model.TopicWithComments;
import com.google.common.base.Optional;
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

public class TopicParser extends Parser {
    private static final Pattern PATTERN_CSRF_TOKEN = Pattern.compile("var csrfToken = \"(\\w{32})\"");
    private static final Pattern PATTERN_TOPIC_REPLY_TIME = Pattern.compile("·\\s*(.+?)(?:\\s+·|$)");
    private static final Pattern PATTERN_POSTSCRIPT = Pattern.compile("·\\s+(.+)");
    private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\d+");

    public static TopicWithComments parseDoc(Document doc, Topic topic) {
        final Topic.Builder topicBuilder = topic.toBuilder();
        final Element mainElement = new JsoupObjects(doc).bfs("body").child("#Wrapper")
                .child(".content").child("#Main").getOne();

        parseTopicInfo(topicBuilder, mainElement);

        List<Comment> comments = parseComments(mainElement);
        int[] pageNum = getMaxPage(mainElement);

        final String csrfToken;
        final String onceToken;
        if (UserState.getInstance().isLoggedIn()) {
            csrfToken = parseCsrfToken(doc);
            onceToken = parseOnceToken(doc);
            topicBuilder.isFavored(parseFavored(mainElement));
        } else {
            csrfToken = null;
            onceToken = null;
        }
        return new TopicWithComments(topicBuilder.createTopic(), comments, pageNum[0],
                pageNum[1], csrfToken, onceToken);
    }

    private static int[] getMaxPage(Element main) {
        final Optional<Element> optional = new JsoupObjects(main).child("div:nth-child(4)")
                .child(".inner:last-child:not([id])").getOptional();
        if (optional.isPresent()) {
            final Element element = optional.get();
            final int maxPage = element.children().size();
            final int curPage = Integer.parseInt(JsoupObjects.child(element, ".page_current").text());

            return new int[]{curPage, maxPage};
        } else {
            return new int[]{1, 1};
        }
    }

    private static String parseCsrfToken(Document doc) {
        final JsoupObjects scripts = new JsoupObjects(doc).bfs("head").child("script");
        for (Element script : scripts) {
            final Matcher matcher = PATTERN_CSRF_TOKEN.matcher(script.html());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    private static String parseOnceToken(Element main) {
        final Optional<Element> optional = new JsoupObjects(main).child(".box:nth-child(6)")
                .child(".cell:nth-child(2)").child("form").child("[name=once]").getOptional();
        if (optional.isPresent()) {
            return optional.get().val();
        }

        return null;
    }

    private static boolean parseFavored(Element main) {
        final Element ele = new JsoupObjects(main).child(".box").child(".topic_buttons").child(".tb").getOne();

        return ele.attr("href").startsWith("/unfav");
    }

    private static void parseTopicInfo(Topic.Builder builder, Element main) {
        final Element topicBox = JsoupObjects.child(main, ".box");
        final Element header = JsoupObjects.child(topicBox, ".header");

        {
            final Node node = TopicListParser.parseNode(new JsoupObjects(header).child(".chevron").adjacent("a").getOne());
            builder.setNode(node);
        }
        parseTopicReplyTime(builder, JsoupObjects.child(header, ".gray").textNodes().get(0));
        parseTopicTitle(builder, header);

        parseTopicContent(builder, topicBox);
        parsePostscript(builder, topicBox);
        parseTopicReplyCount(builder, main);


        if (!builder.hasInfo()) {
            TopicListParser.parseMember(builder, JsoupObjects.child(header, ".fr"));
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

    private static void parseTopicTitle(Topic.Builder builder, Element header) {
        final String title = JsoupObjects.child(header, "h1").html();

        builder.setTitle(title);
    }

    private static void parseTopicReplyCount(Topic.Builder topicBuilder, Element main) {
        final Optional<Element> optional = new JsoupObjects(main).child(":nth-child(4)").child(".cell").child(".gray").getOptional();
        if (optional.isPresent()) {
            final String text = optional.get().ownText();
            final Matcher matcher = PATTERN_NUMBERS.matcher(text);
            Preconditions.checkState(matcher.find());

            topicBuilder.setReplyCount(Integer.parseInt(matcher.group()));
        } else {
            // empty reply
            topicBuilder.setReplyCount(0);
        }
    }

    private static List<Comment> parseComments(Element main) {
        final JsoupObjects elements = new JsoupObjects(main).child(":nth-child(4)").child("div").child("table").child("tbody").child("tr");
        return Lists.newArrayList(Iterables.transform(elements, (ele) -> {
            final Avatar.Builder avatarBuilder = new Avatar.Builder();
            parseAvatar(avatarBuilder, ele);

            final Element td = JsoupObjects.child(ele, "td:nth-child(3)");

            final Member.Builder memberBuilder = new Member.Builder();
            memberBuilder.setAvatar(avatarBuilder.createAvatar());
            parseMember(memberBuilder, td);

            final Comment.Builder commentBuilder = new Comment.Builder();
            commentBuilder.setMember(memberBuilder.createMember());

            parseCommentInfo(commentBuilder, td);
            parseCommentContent(commentBuilder, td);

            return commentBuilder.createComment();
        }));
    }

    private static void parseCommentContent(Comment.Builder builder, Element ele) {
        builder.setContent(JsoupObjects.child(ele, ".reply_content").html());
    }

    private static void parseCommentInfo(Comment.Builder builder, Element ele) {
        final Element tableEle = JsoupObjects.parents(ele, "div");
        // example data: r_123456
        final int id = Integer.parseInt(tableEle.id().substring(2));
        builder.setId(id);

        final Element fr = JsoupObjects.child(ele, ".fr");
        builder.setThanked(Iterables.any(new JsoupObjects(fr).child(".thank_area"), e -> e.hasClass("thanked")));
        builder.setFloor(Integer.parseInt(JsoupObjects.child(fr, ".no").text()));

        final List<Element> elements = new JsoupObjects(ele).child(".small").getList();

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
        builder.setUsername(new JsoupObjects(ele).bfs(".dark").getOne().text());
    }

    private static void parseAvatar(Avatar.Builder builder, Element ele) {
        builder.setUrl(new JsoupObjects(ele).dfs(".avatar").getOne().attr("src"));
    }

    private static void parseTopicContent(Topic.Builder builder, Element topicBox) {
        final Optional<Element> optional = new JsoupObjects(topicBox).child(".cell").child(".topic_content").getOptional();
        if (optional.isPresent()) {
            final Element element = optional.get();
            builder.setContent(element.html());
        }
    }

    private static void parsePostscript(Topic.Builder builder, Element topicBox) {
        final Iterable<Element> elements = new JsoupObjects(topicBox).child(".subtle");
        final Iterable<Postscript> subtles = Iterables.transform(elements, ele -> {
            final Element fade = JsoupObjects.child(ele, ".fade");
            final Matcher matcher = PATTERN_POSTSCRIPT.matcher(fade.text());
            Preconditions.checkArgument(matcher.find());
            final String time = matcher.group(1);

            final String content = JsoupObjects.child(ele, ".topic_content").html();

            return new Postscript(content, time);
        });

        builder.setPostscripts(Lists.newArrayList(subtles));
    }

    public static String parseProblemInfo(String html) {
        final Document doc = Parser.toDoc(html);
        final Elements elements = doc.select(".problem ul:first-child");
        Preconditions.checkState(elements.size() == 1, "problem size isn't one");

        return elements.get(0).html();
    }
}
