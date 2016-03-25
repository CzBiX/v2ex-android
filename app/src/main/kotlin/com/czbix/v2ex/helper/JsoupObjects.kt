package com.czbix.v2ex.helper

import android.util.LruCache
import com.czbix.v2ex.common.exception.FatalException
import com.google.common.base.Optional
import com.google.common.collect.TreeTraverser
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator
import java.lang.reflect.Method
import java.util.*

/**
 * Jsoup use bottom-up parsing to find element, it's slow when we only used a few elements.
 * This class provided a more direct way to find elements.
 */
class JsoupObjects : Iterable<Element> {
    private var mResult: Sequence<Element>

    constructor(vararg elements: Element) {
        mResult = elements.asSequence()
    }

    /**
     * get one element and remove it.
     * @throws NoSuchElementException result is empty
     * @see .getOptional
     */
    val one: Element
        @Throws(NoSuchElementException::class)
        get() = mResult.first()

    /**
     * get one element and remove it
     * @see .getOne
     */
    val optional: Optional<Element>
        get() = Optional.fromNullable(mResult.firstOrNull())

    private fun addOneQuery(evaluator: Evaluator, getElement: (Element) -> Element?) {
        mResult = mResult.mapNotNull { ele ->
            getElement(ele)?.let {
                if (evaluator(it)) it else null
            }
        }
    }

    private fun addQuery(evaluator: Evaluator, getElements: (Element) -> Sequence<Element>) {
        mResult = mResult.flatMap { ele ->
            getElements(ele).filter { e ->
                evaluator(e)
            }
        }
    }

    infix fun child(query: String): JsoupObjects {
        val evaluator = parseQuery(query)
        addQuery(evaluator) { it.children().asSequence() }
        return this
    }

    /**
     * find elements by pre-order depth-first-search
     * @see .bfs
     */
    infix fun dfs(query: String): JsoupObjects {
        val evaluator = parseQuery(query)
        addQuery(evaluator) {
            TREE_TRAVERSER.preOrderTraversal(it).asSequence()
        }
        return this
    }

    fun head(): JsoupObjects = bfs("head")

    fun body(): JsoupObjects = bfs("body")

    /**
     * find elements by breadth-first-search
     * @see .dfs
     */
    infix fun bfs(query: String): JsoupObjects {
        val evaluator = parseQuery(query)
        addQuery(evaluator) {
            TREE_TRAVERSER.breadthFirstTraversal(it).asSequence()
        }
        return this
    }

    infix fun parents(query: String): JsoupObjects {
        val evaluator = parseQuery(query)
        addQuery(evaluator) {
            PARENT_TRAVERSER.breadthFirstTraversal(it).asSequence()
        }
        return this
    }

    infix fun adjacent(query: String): JsoupObjects {
        val evaluator = parseQuery(query)
        addOneQuery(evaluator) {
            it.nextElementSibling()
        }
        return this
    }

    override fun iterator(): Iterator<Element> = mResult.iterator()

    private operator fun Evaluator.invoke(e: Element) = this.matches(e, e)

    companion object {
        private val PARSE_METHOD: Method
        private val EVALUATOR_LRU_CACHE: LruCache<String, Evaluator>

        init {
            try {
                // this class isn't public.
                val queryParserCls = Class.forName("org.jsoup.select.QueryParser")
                val parseMethod = queryParserCls.getDeclaredMethod("parse", String::class.java)
                parseMethod.isAccessible = true
                PARSE_METHOD = parseMethod
            } catch (e: Exception) {
                throw FatalException("get QueryParser.parse failed", e)
            }

            EVALUATOR_LRU_CACHE = object : LruCache<String, Evaluator>(64) {
                override fun create(key: String): Evaluator {
                    try {
                        return PARSE_METHOD.invoke(null, key) as Evaluator
                    } catch (e: Exception) {
                        throw FatalException("invoke QueryParser.parse failed", e)
                    }
                }
            }
        }

        @JvmStatic
        fun child(ele: Element, query: String): Element {
            return JsoupObjects(ele).child(query).first();
        }

        @JvmStatic
        fun parents(ele: Element, query: String): Element {
            return JsoupObjects(ele).parents(query).first();
        }

        private fun parseQuery(query: String) = EVALUATOR_LRU_CACHE.get(query)

        private val TREE_TRAVERSER = object : TreeTraverser<Element>() {
            override fun children(root: Element): Iterable<Element> {
                return root.children()
            }
        }

        private val PARENT_TRAVERSER = object : TreeTraverser<Element>() {
            override fun children(root: Element): Iterable<Element> {
                return listOf(root.parent())
            }
        }
    }
}