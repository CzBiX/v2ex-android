package com.czbix.v2ex.helper

import android.util.LruCache
import com.google.common.graph.Traverser
import org.jsoup.nodes.Element
import org.jsoup.select.Evaluator
import org.jsoup.select.QueryParser

/**
 * Jsoup use bottom-up parsing to find element, it's slow when we only used a few elements.
 * This class provided a more direct way to find elements.
 */
class JsoupObjects(vararg elements: Element) : Iterable<Element> {
    private var mResult: Sequence<Element>

    init {
        mResult = elements.asSequence()
    }

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


    fun child(vararg queries: String): JsoupObjects {
        return queries.fold(this) { thiz, query ->
            thiz.child(query)
        }
    }

    /**
     * find elements by pre-order depth-first-search
     * @see .bfs
     */
    infix fun dfs(query: String): JsoupObjects {
        val evaluator = parseQuery(query)
        addQuery(evaluator) {
            TREE_TRAVERSER.depthFirstPreOrder(it).asSequence()
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
            TREE_TRAVERSER.breadthFirst(it).asSequence()
        }
        return this
    }

    infix fun parents(query: String): JsoupObjects {
        val evaluator = parseQuery(query)
        addQuery(evaluator) {
            PARENT_TRAVERSER.breadthFirst(it).asSequence()
        }
        return this
    }

    infix fun adjacent(query: String): JsoupObjects {
        val evaluator = parseQuery(query)
        addOneQuery(evaluator, Element::nextElementSibling)
        return this
    }

    override fun iterator(): Iterator<Element> = mResult.iterator()

    private operator fun Evaluator.invoke(e: Element) = this.matches(e, e)

    companion object {
        private val EVALUATOR_LRU_CACHE: LruCache<String, Evaluator>

        init {
            EVALUATOR_LRU_CACHE = object : LruCache<String, Evaluator>(64) {
                override fun create(key: String): Evaluator {
                    return QueryParser.parse(key)
                }
            }
        }

        @JvmStatic
        fun Element.query(): JsoupObjects {
            return JsoupObjects(this)
        }

        @JvmStatic
        fun child(ele: Element, query: String): Element {
            return JsoupObjects(ele).child(query).first()
        }

        @JvmStatic
        fun parents(ele: Element, query: String): Element {
            return JsoupObjects(ele).parents(query).first()
        }

        private fun parseQuery(query: String) = EVALUATOR_LRU_CACHE.get(query)

        private val TREE_TRAVERSER = Traverser.forTree<Element> {
            it.children()
        }

        private val PARENT_TRAVERSER = Traverser.forTree<Element> {
            listOf(it.parent())
        }
    }
}