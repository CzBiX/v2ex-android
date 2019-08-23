package com.czbix.v2ex.ui.adapter

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updatePaddingRelative
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.airbnb.epoxy.*
import com.airbnb.epoxy.preload.Preloadable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.Target
import com.czbix.v2ex.R
import com.czbix.v2ex.common.PrefStore
import com.czbix.v2ex.model.*
import com.czbix.v2ex.network.GlideConfig
import com.czbix.v2ex.postscript
import com.czbix.v2ex.ui.ExHolder
import com.czbix.v2ex.ui.fragment.NodeListFragment.OnNodeActionListener
import com.czbix.v2ex.ui.widget.*
import com.czbix.v2ex.ui.widget.AvatarView.OnAvatarActionListener
import com.czbix.v2ex.ui.widget.CommentView.OnCommentActionListener
import com.czbix.v2ex.ui.widget.HtmlMovementMethod.OnHtmlActionListener
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
    private var author: Member? = null
    private var commentList: List<Comment>? = null
    var commentBasePos: Int = 0
        private set

    fun setTopic(topic: Topic) {
        this.topic = topic

        requestDelayedModelBuild(100)
    }

    fun setData(author: Member?, comments: List<Comment>?) {
        this.author = author
        this.commentList = comments
        requestModelBuild()
    }

    override fun buildModels() {
        if (!topic.hasInfo) {
            return
        }

        commentControllerTopic {
            id("topic")
            nodeListener(mNodeListener)
            avatarListener(mAvatarListener)
            topic(topic)
            hasContent(!topic.content.isNullOrEmpty())
        }

        topic.content?.run {
            val hasPostscript = topic.postscripts?.run { size > 0 } ?: false

            addBlocks(this) {index ->
                !hasPostscript && index == lastIndex
            }
        }

        topic.postscripts?.run {
            forEachIndexed { index, postscript ->
                val lastPostscript = index == lastIndex
                postscript {
                    id(index)
                    index(index + 1)
                    time(postscript.time)
                }

                postscript.content.apply {
                    addBlocks(this) {
                        lastPostscript && it == lastIndex
                    }
                }
            }
        }

        commentBasePos = modelCountBuiltSoFar
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

    private inline fun addBlocks(blocks: List<ContentBlock>, showDivider: (index: Int) -> Boolean) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is TextBlock -> {
                    commentControllerTextBlock {
                        id(block.id)
                        text(block.text)
                        showDivider(showDivider(index))
                        contentListener(mContentListener)
                    }
                }
                is ImageBlock -> {
                    commentControllerImageBlock {
                        id(block.id)
                        source(block.source)
                        showDivider(showDivider(index))
                        contentListener(mContentListener)
                    }
                }
                else -> error("Unknown block type.")
            }
        }
    }

    abstract class ContentBlockModel<T : EpoxyHolder> : EpoxyModelWithHolder<T>() {
        @EpoxyAttribute
        open var showDivider = false

        @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
        open lateinit var contentListener: OnHtmlActionListener
    }

    @EpoxyModelClass(layout = R.layout.view_content_text)
    abstract class TextBlockModel : ContentBlockModel<TextBlockModel.Holder>() {
        @EpoxyAttribute
        lateinit var text: CharSequence

        override fun bind(holder: Holder) {
            val view = holder.view
            view.movementMethod = HtmlMovementMethod(contentListener)
            DividerItemDecoration.setHasDivider(view, showDivider)

            view.setText(text, TextView.BufferType.SPANNABLE)
        }

        override fun unbind(holder: Holder) {
            holder.view.text = null
        }

        class Holder : ExHolder<TextView>() {
            override fun init() {
                if (PrefStore.getInstance().isContentSelectable) {
                    view.setTextIsSelectable(true)
                }
            }
        }
    }

    @EpoxyModelClass(layout = R.layout.view_content_image)
    abstract class ImageBlockModel : ContentBlockModel<ImageBlockModel.Holder>(), View.OnClickListener {
        @EpoxyAttribute
        lateinit var source: String

        override fun bind(holder: Holder) {
            val view = holder.view
            DividerItemDecoration.setHasDivider(view, showDivider)

            view.setOnClickListener(this)

            val request = getImgRequest(holder.glide)
                    .placeholder(holder.loadingDrawable)
                    .error(holder.errorDrawable)

            val shouldLoadImage = PrefStore.getInstance().shouldLoadImage()
            if (!shouldLoadImage) {
                request.load(holder.disabledDrawable)
            } else {
                request.listener(getGlideListener(view))
            }.into(holder.target)
        }

        fun getImgRequest(glide: RequestManager): RequestBuilder<Drawable> {
            val strategy = GlideConfig.atWidthMost()

            return glide.asDrawable().downsample(strategy)
                    .optionalTransform(GlideConfig.AtWidthMostTransformation(strategy)).load(source)
        }

        override fun unbind(holder: Holder) {
            holder.glide.clear(holder.view)
        }

        override fun onClick(v: View?) {
            contentListener.onImageClick(source)
        }

        class ImageTarget(view: ImageView) : DrawableImageViewTarget(view) {
            private fun resetState() {
                view.apply {
                    scaleType = ImageView.ScaleType.CENTER
                    adjustViewBounds = false
                    minimumHeight = ViewUtils.dp2Px(40)
                }
            }

            override fun onLoadStarted(placeholder: Drawable?) {
                resetState()
                super.onLoadStarted(placeholder)

                if (placeholder is Animatable) {
                    // HACK: let placeholder animated
                    onResourceReady(placeholder, null)
                }
            }
        }

        class Holder : ExHolder<ImageView>(), Preloadable {
            val loadingDrawable by lazy {
                CircularProgressDrawable(view.context).apply {
                    centerRadius = ViewUtils.dp2Px(10f)
                    strokeWidth = ViewUtils.dp2Px(2f)
                }
            }
            val errorDrawable by lazy {
                view.context.getDrawable(R.drawable.ic_sync_problem_white_24dp)!!.apply {
                    setTint(Color.BLACK)
                }
            }
            val disabledDrawable by lazy {
                view.context.getDrawable(R.drawable.ic_sync_disabled_white_24dp)!!.apply {
                    setTint(Color.BLACK)
                }
            }
            override val viewsToPreload by lazy { listOf(view) }
            val target by lazy { ImageTarget(view) }
        }

        companion object {
            fun getGlideListener(view: ImageView): RequestListener<Drawable> {
                return object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any?, target: Target<Drawable>, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        val maxWidth = runBlocking {
                            suspendCoroutine<Float> { cont ->
                                target.getSize { width, _ ->
                                    cont.resume(width.toFloat())
                                }
                            }
                        }

                        val width = resource.intrinsicWidth

                        val scale = if (width < maxWidth) {
                            if (ViewUtils.dp2Px(width.toFloat()) < maxWidth) {
                                ViewUtils.density
                            } else {
                                maxWidth / width
                            }
                        } else {
                            1f
                        }

                        view.apply {
                            scaleType = ImageView.ScaleType.MATRIX
                            adjustViewBounds = true
                            minimumHeight = 0

                            imageMatrix = Matrix().apply {
                                preScale(scale, scale)
                                preTranslate((maxWidth - width * scale) / 2, 0f)
                            }
                        }

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
            val view = holder.view

            if (hasContent) {
                view.updatePaddingRelative(bottom = 0)
            }

            view.setNodeListener(nodeListener)
            view.setAvatarListener(avatarListener)

            view.fillData(holder.glide, topic)
            DividerItemDecoration.setHasDivider(view, !hasContent)
        }

        override fun unbind(holder: Holder) {
            holder.view.clear(holder.glide)
        }

        class Holder : ExHolder<TopicView>() {
            override fun init() {
                view.updatePaddingRelative(
                        top = view.paddingTop * 2,
                        bottom = view.paddingBottom * 2
                )
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
            val view = holder.view

            view.setListener(listener)
            view.fillData(holder.glide, comment, isAuthor, position)
        }

        override fun unbind(holder: Holder) {
            holder.view.clear(holder.glide)
        }

        fun getImgRequest(glide: RequestManager, avatarView: AvatarView): RequestBuilder<Drawable> {
            return avatarView.getImgRequest(glide, comment.member.avatar!!)
        }

        class Holder : ExHolder<CommentView>(), Preloadable {
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
    }
}
