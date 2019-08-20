package com.czbix.v2ex.ui.adapter

import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.getSpans
import androidx.core.view.updatePaddingRelative
import com.airbnb.epoxy.*
import com.airbnb.epoxy.preload.Preloadable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.czbix.v2ex.R
import com.czbix.v2ex.common.PrefStore
import com.czbix.v2ex.databinding.ViewPostscriptBinding
import com.czbix.v2ex.model.Comment
import com.czbix.v2ex.model.Member
import com.czbix.v2ex.model.Postscript
import com.czbix.v2ex.model.Topic
import com.czbix.v2ex.network.GlideConfig
import com.czbix.v2ex.ui.ExHolder
import com.czbix.v2ex.ui.fragment.NodeListFragment.OnNodeActionListener
import com.czbix.v2ex.ui.widget.*
import com.czbix.v2ex.ui.widget.AvatarView.OnAvatarActionListener
import com.czbix.v2ex.ui.widget.CommentView.OnCommentActionListener
import com.czbix.v2ex.ui.widget.HtmlMovementMethod.OnHtmlActionListener
import com.czbix.v2ex.util.MiscUtils
import com.czbix.v2ex.util.ViewUtils
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CommentController(
        private val mCommentListener: OnCommentActionListener,
        private val mContentListener: OnHtmlActionListener,
        private val mNodeListener: OnNodeActionListener,
        private val mAvatarListener: OnAvatarActionListener
) : EpoxyController() {
    private lateinit var topic: Topic
    private var topicBlocks: List<ContentBlock>? = null
    private var author: Member? = null
    private var commentList: List<Comment>? = null

    fun setTopic(topic: Topic, topicBlocks: List<ContentBlock>?) {
        this.topic = topic
        this.topicBlocks = topicBlocks

        requestDelayedModelBuild(100)
    }

    fun setData(author: Member?, comments: List<Comment>?) {
        this.author = author
        this.commentList = comments
        requestModelBuild()
    }

    override fun buildModels() {
        if (!topic.hasInfo()) {
            return
        }

        commentControllerTopic {
            id("topic")
            nodeListener(mNodeListener)
            avatarListener(mAvatarListener)
            topic(topic)
            hasContent(!topic.content.isNullOrEmpty())
        }

        topicBlocks?.run {
            val hasPostscript = topic.postscripts?.run { size > 0 } ?: false

            forEachIndexed { index, block ->
                when (block) {
                    is TextBlock -> {
                        commentControllerTextBlock {
                            id(block.id)
                            text(block.text)
                            hasPostscript(hasPostscript)
                            isTheLast(index == lastIndex)
                            contentListener(mContentListener)
                        }
                    }
                    is ImageBlock -> {
                        commentControllerImageBlock {
                            id(block.id)
                            source(block.source)
                            hasPostscript(hasPostscript)
                            isTheLast(index == lastIndex)
                            contentListener(mContentListener)
                        }
                    }
                }
            }
        }

        topic.postscripts?.run {
            forEachIndexed { index, postscript ->
                commentControllerPostscript {
                    id(index)
                    index(index)
                    postscript(postscript)
                    contentListener(mContentListener)
                    isLastPostscript(index == lastIndex)
                }
            }
        }

        commentList?.forEachIndexed { index, comment ->
            commentControllerComment {
                id(comment.id)
                listener(mCommentListener)
                comment(comment)
                isAuthor(comment.member.isSameUser(author!!))
                position(index)
            }
        }
    }

    abstract class ContentBlock(val id: Int)
    class TextBlock(id: Int, val text: CharSequence) : ContentBlock(id)
    class ImageBlock(id: Int, val source: String) : ContentBlock(id)

    abstract class ContentBlockModel<T : EpoxyHolder> : EpoxyModelWithHolder<T>() {
        @EpoxyAttribute
        open var isTheLast = false

        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        open lateinit var contentListener: OnHtmlActionListener
    }

    abstract class TopicBlockModel<T : EpoxyHolder> : ContentBlockModel<T>() {
        @EpoxyAttribute
        open var hasPostscript = false
    }

    @EpoxyModelClass(layout = R.layout.view_topic_text)
    abstract class TextBlockModel : TopicBlockModel<TextBlockModel.Holder>() {
        @EpoxyAttribute
        lateinit var text: CharSequence

        override fun bind(holder: Holder) {
            val view = holder.view

            DividerItemDecoration.setHasDecoration(view, !hasPostscript && isTheLast)
            view.setText(text, TextView.BufferType.SPANNABLE)
        }

        override fun unbind(holder: Holder) {
            holder.view.text = null
        }

        inner class Holder : ExHolder<TextView>() {
            override fun init() {
                view.movementMethod = HtmlMovementMethod(contentListener)

                if (PrefStore.getInstance().isContentSelectable) {
                    view.setTextIsSelectable(true)
                }
            }
        }
    }

    @EpoxyModelClass(layout = R.layout.view_topic_image)
    abstract class ImageBlockModel : TopicBlockModel<ImageBlockModel.Holder>(), View.OnClickListener {
        @EpoxyAttribute
        lateinit var source: String

        override fun bind(holder: Holder) {
            val view = holder.view
            DividerItemDecoration.setHasDecoration(view, !hasPostscript && isTheLast)

            view.setOnClickListener(this)

            val anim = ObjectAnimator.ofInt(view, "ImageLevel", 0, 3)
            anim.repeatCount = ObjectAnimator.INFINITE
            anim.start()

            view.scaleType = ImageView.ScaleType.FIT_CENTER
            getImgRequest(holder.glide).listener(getGlideListener(view, anim)).into(view)
        }

        fun getImgRequest(glide: RequestManager): RequestBuilder<Drawable> {
            val strategy = GlideConfig.atWidthMost()

            return glide.asDrawable().placeholder(R.drawable.ic_sync_white_24dp).downsample(strategy)
                    .transform(GlideConfig.AtWidthMostTransformation(strategy)).load(source)
        }

        override fun unbind(holder: Holder) {
            holder.glide.clear(holder.view)
        }

        override fun onClick(v: View?) {
            contentListener.onImageClick(source)
        }

        class Holder : ExHolder<ImageView>(), Preloadable {
            override val viewsToPreload by lazy { listOf(view) }
        }

        companion object {
            fun getGlideListener(view: ImageView, animator: ObjectAnimator): RequestListener<Drawable> {
                return object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        animator.cancel()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any?, target: Target<Drawable>, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        animator.cancel()

                        val maxWidth = runBlocking {
                            suspendCoroutine<Float> { cont ->
                                target.getSize { width, _ ->
                                    cont.resume(width.toFloat())
                                }
                            }
                        }

                        val width = resource.intrinsicWidth

                        val scale = if (width < maxWidth) {
                            if (ViewUtils.dp2Pixel(width.toFloat()) < maxWidth) {
                                ViewUtils.density
                            } else {
                                maxWidth / width
                            }
                        } else {
                            1f
                        }

                        view.scaleType = ImageView.ScaleType.MATRIX
                        view.imageMatrix.setScale(scale, scale)

                        return false
                    }
                }
            }
        }
    }

    @EpoxyModelClass(layout = R.layout.view_topic)
    abstract class TopicModel : EpoxyModelWithHolder<TopicModel.Holder>() {
        @EpoxyAttribute
        lateinit var topic: Topic

        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        lateinit var nodeListener: OnNodeActionListener

        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        lateinit var avatarListener: OnAvatarActionListener

        @EpoxyAttribute
        var hasContent = false

        override fun bind(holder: Holder) {
            holder.view.fillData(holder.glide, topic)
            DividerItemDecoration.setHasDecoration(holder.view, !hasContent)
        }

        override fun unbind(holder: Holder) {
            holder.view.clear(holder.glide)
        }

        inner class Holder : ExHolder<TopicView>() {
            override fun init() {
                view.updatePaddingRelative(top = view.paddingTop * 2)

                view.setNodeListener(nodeListener)
                view.setAvatarListener(avatarListener)
            }
        }
    }

    @EpoxyModelClass(layout = R.layout.view_postscript)
    abstract class PostscriptModel : EpoxyModelWithHolder<PostscriptModel.Holder>() {
        @EpoxyAttribute
        lateinit var postscript: Postscript

        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        lateinit var contentListener: OnHtmlActionListener

        @EpoxyAttribute
        var index = 0

        @EpoxyAttribute
        var isLastPostscript = false

        override fun bind(holder: Holder) {
            val binding = holder.binding
            val context = holder.view.context
            binding.setTitle(context.getString(R.string.title_postscript, index + 1))
            binding.setTime(postscript.mTime)

            ViewUtils.setHtmlIntoTextView(binding.content, postscript.mContent, 0, true)
            holder.contentView.movementMethod = HtmlMovementMethod(contentListener)

            DividerItemDecoration.setHasDecoration(holder.view, isLastPostscript)
        }

        override fun unbind(holder: Holder) {
            holder.contentView.text = null
        }

        inner class Holder : ExHolder<View>() {
            lateinit var binding: ViewPostscriptBinding
            val contentView by lazy {
                binding.content
            }

            override fun init() {
                binding = ViewPostscriptBinding.bind(view)

                ViewUtils.setSpannableFactory(binding.content)
            }
        }
    }

    @EpoxyModelClass(layout = R.layout.view_comment)
    abstract class CommentModel : EpoxyModelWithHolder<CommentModel.Holder>() {
        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        lateinit var listener: OnCommentActionListener

        @EpoxyAttribute
        lateinit var comment: Comment

        @EpoxyAttribute
        var isAuthor = false

        @EpoxyAttribute
        var position = 0

        override fun bind(holder: Holder) {
            holder.view.fillData(holder.glide, comment, isAuthor, position)
        }

        override fun unbind(holder: Holder) {
            holder.view.clear(holder.glide)
        }

        fun getImgRequest(glide: RequestManager, avatarView: AvatarView): RequestBuilder<Drawable> {
            return avatarView.getImgRequest(glide, comment.member.avatar!!)
        }

        inner class Holder : ExHolder<CommentView>(), Preloadable {
            override fun init() {
                view.setListener(listener)
            }

            override val viewsToPreload by lazy {
                listOf(view.mAvatar)
            }
        }
    }

    companion object {
        fun addGlidePreloader(recyclerView: EpoxyRecyclerView, glide: RequestManager) {
            recyclerView.addGlidePreloader(glide,
                    preloader = glidePreloader()
                    { requestManager, epoxyModel: `CommentController$ImageBlockModel_`, _ ->
                        epoxyModel.getImgRequest(requestManager)
                    })
            recyclerView.addGlidePreloader(glide, 5,
                    preloader = glidePreloader(viewMetadata = { view ->
                        AvatarView.Metadata(view as AvatarView)
                    }) { requestManager, epoxyModel: `CommentController$CommentModel_`, viewData ->
                        epoxyModel.getImgRequest(requestManager, viewData.metadata.avatarView)
                    }
            )
        }

        fun parseHtml2Blocks(html: String): List<ContentBlock> {
            val builder = ViewUtils.parseHtml(html, null, true)
            val simpleResult by lazy {
                val block = TextBlock(0, builder)

                listOf(block)
            }

            if (builder !is Spanned) {
                return simpleResult
            }

            val imageSpans = builder.getSpans<ImageSpan>()
            if (imageSpans.isEmpty()) {
                return simpleResult
            }

            var lastEndPos = 0
            var index = 0
            val blocks = mutableListOf<ContentBlock>()
            imageSpans.forEach { span ->
                val start = builder.getSpanStart(span)
                val text = builder.subSequence(lastEndPos, start).trim()
                if (text.isNotEmpty()) {
                    blocks.add(TextBlock(index++, text))
                }

                val url = MiscUtils.formatUrl(span.source!!)
                blocks.add(ImageBlock(index++, url))

                lastEndPos = builder.getSpanEnd(span)
            }

            val length = builder.length
            if (lastEndPos != length) {
                val text = builder.subSequence(lastEndPos, length).trim()

                blocks.add(TextBlock(index, text))
            }

            return blocks
        }
    }
}
