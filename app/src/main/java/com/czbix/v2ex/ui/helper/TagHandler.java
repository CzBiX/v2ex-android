package com.czbix.v2ex.ui.helper;

import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.URLSpan;

import com.czbix.v2ex.common.exception.FatalException;
import com.czbix.v2ex.util.LogUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagHandler implements Html.TagHandler {
    private static final String TAG = TagHandler.class.getSimpleName();
    private static final Pattern PATTERN_GIST = Pattern.compile("(https?://gist.github.com/.*?).js");
    private static final Pattern PATTERN_YOUTUBE = Pattern.compile("//www.youtube.com/embed/(.*)");
    private static final TagHandler instance;

    static {
        instance = new TagHandler();
    }

    public static TagHandler getInstance() {
        return instance;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        LogUtils.v(TAG, "%s %s", opening ? "opening" : "ending", tag);

        try {
            if (opening) {
                handleOpening(tag, output, xmlReader);
            } else {
                handleEnding(tag, output, xmlReader);
            }
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            throw new FatalException(e);
        }
    }

    private void handleOpening(String tag, Editable output, XMLReader xmlReader) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (tag.equalsIgnoreCase("script")) {
            startScript(output, xmlReader);
        } else if (tag.equalsIgnoreCase("iframe")) {
            startIframe(output, xmlReader);
        }
    }

    private void handleEnding(String tag, Editable output, XMLReader xmlReader) {
        if (tag.equalsIgnoreCase("script")) {
            endScript(output);
        } else if (tag.equalsIgnoreCase("iframe")) {
            endIframe(output);
        }
    }

    private void startScript(Editable text, XMLReader xmlReader) throws SAXNotRecognizedException, SAXNotSupportedException {
        String url = getAttribute(xmlReader, "src");

        int length = text.length();
        text.setSpan(new Script(url), length, length, Spanned.SPAN_MARK_MARK);
    }

    private void endScript(Editable text) {
        Object obj = getLast(text, Script.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        Script script = ((Script) obj);
        Preconditions.checkNotNull(script);
        String url = script.mUrl;
        if (Strings.isNullOrEmpty(url)) {
            return;
        }

        Matcher matcher = PATTERN_GIST.matcher(url);
        if (matcher.matches()) {
            // remove the js ext for gist
            url = matcher.group(1);
        }

        text.insert(where, url);
        int length = text.length();

        text.setSpan(new URLSpan(url), where, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void startIframe(Editable text, XMLReader xmlReader) throws SAXNotRecognizedException, SAXNotSupportedException {
        String url = getAttribute(xmlReader, "src");

        int length = text.length();
        text.setSpan(new Iframe(url), length, length, Spanned.SPAN_MARK_MARK);
    }

    private void endIframe(Editable text) {
        Object obj = getLast(text, Iframe.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        Iframe iframe = ((Iframe) obj);
        Preconditions.checkNotNull(iframe);
        String url = iframe.mUrl;
        if (Strings.isNullOrEmpty(url)) {
            return;
        }

        Matcher matcher = PATTERN_YOUTUBE.matcher(url);
        if (matcher.matches()) {
            // replace with youtube full url
            url = String.format("https://www.youtube.com/watch?v=%s", matcher.group(1));
        }

        text.insert(where, url);
        int length = text.length();

        text.setSpan(new URLSpan(url), where, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static <T> Object getLast(Spanned text, Class<T> kind) {
    /*
     * This knows that the last returned object from getSpans()
     * will be the most recently added.
     */
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private static void start(Editable text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    private static <T> void end(Editable text, Class<T> kind,
                                Object repl) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static class Iframe {
        public final String mUrl;

        public Iframe(String url) {
            mUrl = url;
        }
    }

    private static class Script {
        public final String mUrl;

        public Script(String url) {
            mUrl = url;
        }
    }

    private static String getAttribute(XMLReader xmlReader, String name) {
        try {
            Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
            elementField.setAccessible(true);
            Object element = elementField.get(xmlReader);
            Field attsField = element.getClass().getDeclaredField("theAtts");
            attsField.setAccessible(true);
            Object atts = attsField.get(element);
            Field dataField = atts.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            String[] data = (String[]) dataField.get(atts);
            Field lengthField = atts.getClass().getDeclaredField("length");
            lengthField.setAccessible(true);
            int len = (Integer) lengthField.get(atts);

            for(int i = 0; i < len; i++) {
                if(name.equals(data[i * 5 + 1])) {
                    return data[i * 5 + 4];
                }
            }
        } catch (Exception e) {
            LogUtils.i(TAG, "get attribute failed", e);
        }

        return null;
    }
}
