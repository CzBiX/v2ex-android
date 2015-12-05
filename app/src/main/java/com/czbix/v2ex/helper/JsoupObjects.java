package com.czbix.v2ex.helper;

import android.support.annotation.NonNull;
import android.util.LruCache;

import com.czbix.v2ex.common.exception.FatalException;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;

import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Jsoup use bottom-up parsing to find element, it's slow when we only used a few elements.
 * This class provided a more direct way to find elements.
 */
public class JsoupObjects implements Iterable<Element> {
    private static final Method PARSE_METHOD;
    private static final LruCache<String, Evaluator> EVALUATOR_LRU_CACHE;

    static {
        try {
            // this class isn't public.
            final Class<?> queryParserCls = Class.forName("org.jsoup.select.QueryParser");
            final Method parseMethod = queryParserCls.getDeclaredMethod("parse", String.class);
            parseMethod.setAccessible(true);
            PARSE_METHOD = parseMethod;
        } catch (Exception e) {
            throw new FatalException("get QueryParser#parse failed", e);
        }
        EVALUATOR_LRU_CACHE = new LruCache<>(64);
    }

    private FluentIterable<Element> mResult;

    public JsoupObjects(Element... elements) {
        mResult = FluentIterable.of(elements);
    }

    public JsoupObjects(Iterable<Element> elements) {
        mResult = FluentIterable.from(elements);
    }

    /**
     * get one element and remove it.
     * @throws NoSuchElementException result is empty
     * @see #getOptional()
     */
    public Element getOne() {
        final Optional<Element> first = mResult.first();
        if (!first.isPresent()) {
            throw new NoSuchElementException();
        }

        return first.get();
    }

    /**
     * get one element and remove it
     * @see #getOne()
     */
    public Optional<Element> getOptional() {
        return mResult.first();
    }

    /**
     * get all result as {@link List<Element>}
     */
    public List<Element> getList() {
        return mResult.toList();
    }

    private Iterable<Element> filterByEvaluator(Iterable<Element> iterator, Evaluator evaluator) {
        return Iterables.filter(iterator, e -> evaluator.matches(e, e));
    }

    private void addQuery(Function<Element, Iterable<Element>> getElements, Evaluator evaluator) {
        //noinspection ConstantConditions
        mResult = mResult.transformAndConcat(ele -> filterByEvaluator(getElements.apply(ele), evaluator));
    }

    public static Element child(Element ele, String query) {
        return new JsoupObjects(ele).child(query).getOne();
    }

    public JsoupObjects child(String query) {
        final Evaluator evaluator = parseQuery(query);
        addQuery(Element::children, evaluator);
        return this;
    }

    /**
     * find elements by pre-order depth-first-search
     * @see #bfs(String)
     */
    public JsoupObjects dfs(String query) {
        final Evaluator evaluator = parseQuery(query);
        addQuery(TREE_TRAVERSER::preOrderTraversal, evaluator);
        return this;
    }

    /**
     * find elements by breadth-first-search
     * @see #dfs(String)
     */
    public JsoupObjects bfs(String query) {
        final Evaluator evaluator = parseQuery(query);
        addQuery(TREE_TRAVERSER::breadthFirstTraversal, evaluator);
        return this;
    }

    public JsoupObjects adjacent(String query) {
        final Evaluator evaluator = parseQuery(query);
        addQuery(ele -> Lists.newArrayList(ele.nextElementSibling()), evaluator);
        return this;
    }

    @NonNull
    private static Evaluator parseQuery(String query) {
        Evaluator evaluator = EVALUATOR_LRU_CACHE.get(query);
        if (evaluator == null) {
            try {
                evaluator = (Evaluator) PARSE_METHOD.invoke(null, query);
                EVALUATOR_LRU_CACHE.put(query, evaluator);
                return evaluator;
            } catch (Exception e) {
                throw new FatalException("invoke QueryParser#parse failed", e);
            }
        }

        return evaluator;
    }

    @Override
    public Iterator<Element> iterator() {
        return mResult.iterator();
    }

    private static final TreeTraverser<Element> TREE_TRAVERSER = new TreeTraverser<Element>() {
        @Override
        public Iterable<Element> children(@NonNull Element root) {
            return root.children();
        }
    };
}
