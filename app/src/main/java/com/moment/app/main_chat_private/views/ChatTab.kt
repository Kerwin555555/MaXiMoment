package com.moment.app.main_chat_private.views

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.emoji2.widget.EmojiTextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.moment.app.R
import com.moment.app.databinding.ChatBottomBarBinding
import com.moment.app.ui.uiLibs.MomentRefreshView
import com.moment.app.utils.dp
import com.moment.app.utils.getKeyboardHeight
import com.moment.app.utils.getScreenWidth
import com.moment.app.utils.requestNewSize
import com.moment.app.utils.saveKeyboardHeight
import com.moment.app.utils.setBgEnableStateListDrawable
import com.moment.app.utils.setImageResourceSelectedStateListDrawable
import com.moment.app.utils.setOnAvoidMultipleClicksListener


//https://gitee.com/jiao-shichun/ChatInput
@SuppressLint("ClickableViewAccessibility")
class ChatTab: LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private val binding = ChatBottomBarBinding.inflate(LayoutInflater.from(context), this)
    val adapter = Adapter()
    var keyboardHeight = 0
    var contentView: View? = null
        set(value) {
            field = value
            bindContentView()
        }

    init {
        orientation = VERTICAL
        binding.emojiImageView.setImageResourceSelectedStateListDrawable(
            selectedId = R.mipmap.keyboard,
            unSelectedId = R.mipmap.emotion
        )
        binding.sendButton.setBgEnableStateListDrawable(
            enableId = R.drawable.bg_send_icon,
            disableId = R.drawable.bg_gray
        )
        binding.sendButton.isEnabled = false
        binding.emojiImageView.setOnAvoidMultipleClicksListener({
            if (KeyboardUtils.isSoftInputVisible(context as AppCompatActivity)) {
                lockContentHeight()
                KeyboardUtils.hideSoftInput(binding.editText)
                binding.emojiRv.isVisible = true
                binding.emojiImageView.isSelected = true
                unlockContentHeightDelayed()
            } else {
                if (!binding.emojiRv.isVisible) {
                    KeyboardUtils.hideSoftInput(binding.editText)
                    binding.emojiRv.isVisible = true
                    binding.emojiImageView.isSelected = true
                } else {
                    lockContentHeight()
                    postDelayed({
                        binding.emojiRv.isVisible = false
                        binding.emojiImageView.isSelected = false
                    }, 200)
                    showSoftInput()
                    unlockContentHeightDelayed()
                }
            }
        }, 500)
        binding.editText.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK
                && event.action == KeyEvent.ACTION_UP
            ) {
                KeyboardUtils.hideSoftInput(binding.editText)
            }
            return@setOnKeyListener false
        }
        binding.editText.addTextChangedListener(
            onTextChanged = {txt,_,_,_ ->
                binding.sendButton.isEnabled = !txt?.toString()?.trim().isNullOrEmpty()
        })

        binding.emojiRv.layoutManager = GridLayoutManager(context, 6)
        binding.emojiRv.adapter = adapter
        val mutableList = mutableListOf<String>()
        StringUtils.getStringArray(R.array.chat_emojis).forEach {
            mutableList.add(it)
        }
        adapter.setNewData(mutableList)
        keyboardHeight = getKeyboardHeight()
        binding.editText.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP && binding.emojiRv.isVisible) {
                lockContentHeight()
                postDelayed({
                    binding.emojiRv.isVisible = false
                    binding.emojiImageView.isSelected = false
                            }, 200)
                showSoftInput()
                unlockContentHeightDelayed()
            }
            false
        })

        if (keyboardHeight > 180.dp) {
            binding.emojiRv.requestNewSize(width = -1, height = keyboardHeight)
        }
        KeyboardUtils.registerSoftInputChangedListener((context as AppCompatActivity).window) {
            if (it > 10) {
                if (it > keyboardHeight && it > 180.dp) {
                    saveKeyboardHeight(it)
                    keyboardHeight = it
                    binding.emojiRv.requestNewSize(width = -1, height = keyboardHeight)
                }
            }
        }
    }


    inner class Adapter: BaseQuickAdapter<String, BaseViewHolder>(null) {
        override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder {
            return BaseViewHolder(EmojiTextView(mContext).apply {
                gravity = Gravity.CENTER
                setTextColor(0xff000000.toInt())
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24f)
                layoutParams = RecyclerView.LayoutParams(getScreenWidth()/6, getScreenWidth()/6)
            })
        }

        override fun convert(helper: BaseViewHolder, item: String) {
            (helper.itemView as EmojiTextView).setText(item)
            helper.itemView.setOnAvoidMultipleClicksListener({
                 val len = binding.editText.text.length
                 binding.editText.getText().insert(len, item)
                 //binding.emojiRv.isVisible = false
//
//                lockContentHeight()
//                showSoftInput()
//                postDelayed({
//                    binding.emojiRv.isVisible = false
//                    binding.emojiImageView.isSelected = false
//                }, 200)
//                unlockContentHeightDelayed()
            }, 500)
        }
    }

    fun bindContentView() {
        ((contentView as MomentRefreshView<*>)).onMoveCallback = {
            context?.let {
                if (KeyboardUtils.isSoftInputVisible(context as AppCompatActivity)) {
                    KeyboardUtils.hideSoftInput(binding.editText)
                }
                binding.emojiRv.isVisible = false
                binding.emojiImageView.isSelected = false
            }
        }
    }

    private fun lockContentHeight() {
        val params = contentView?.getLayoutParams() as LayoutParams
        params.height = contentView?.getHeight() ?: 0
        params.weight = 0.0f
    }

    private fun unlockContentHeightDelayed() {
        binding.editText.postDelayed({
            (contentView?.layoutParams as LayoutParams).weight = 1.0f
        }, 200L)
    }

    private fun showSoftInput() {
        binding.editText.requestFocus()
        binding.editText.post { KeyboardUtils.showSoftInput(binding.editText) }
    }

    override fun dispatchKeyEvent(ev: KeyEvent): Boolean {
        if (ev.action == KeyEvent.ACTION_UP && ev.keyCode == KeyEvent.KEYCODE_BACK) {
            if (KeyboardUtils.isSoftInputVisible(context as AppCompatActivity)) {
                KeyboardUtils.hideSoftInput(binding.editText)
            }
            binding.emojiRv.isVisible = false
            binding.emojiImageView.isSelected = false
            return true
        }
        return super.dispatchKeyEvent(ev)
    }

    fun getBinding(): ChatBottomBarBinding {
        return binding
    }
}

val width = getScreenWidth()/6