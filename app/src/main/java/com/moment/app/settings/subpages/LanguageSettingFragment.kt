package com.moment.app.settings.subpages

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LanguageUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.didi.drouter.api.DRouter
import com.didi.drouter.api.Extend
import com.moment.app.MainActivity
import com.moment.app.databinding.FragmentLanguageSettingBinding
import com.moment.app.ui.uiLibs.DataDividerItemDecoration
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.popBackStackNowAllowingStateLoss
import com.moment.app.utils.setOnSingleClickListener
import java.util.Arrays
import java.util.Locale

class LanguageSettingFragment : BaseFragment(){

    private lateinit var binding: FragmentLanguageSettingBinding
    private val adapter by lazy {
        LanguageAdapter()
    }

    private val locEntityList: List<LocEntity> = Arrays.asList(
        LocEntity("English", "English", "en"),
        LocEntity("ไทย", "Thai", "th"),
        LocEntity("Tiếng việt", "Vietnamese", "vi"),
        LocEntity("Bahasa Indonesia", "Indonesian", "in"),
        LocEntity("日本語", "Japanese", "ja"),
        LocEntity("中文（繁體）", "Chinese (Traditional)", "zh"),
        LocEntity("Malay", "Malaysia", "ms"),
        LocEntity("Español", "Spanish", "es"),
        LocEntity("Português", "Portuguese", "pt"),
        LocEntity("Türkçe", "Turkish", "tr"),
        LocEntity("Русский язык", "Russian", "ru"),
        LocEntity("لغة عربية", "Arabic", "ar")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLanguageSettingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isClickable = true

        binding.back.setOnSingleClickListener({
            (context as? AppCompatActivity?)?.supportFragmentManager?.popBackStackNowAllowingStateLoss()
        }, 500)

        binding.rv.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.rv.setAdapter(adapter)
        adapter.setNewData(locEntityList)
        adapter.onItemClickListener =
            BaseQuickAdapter.OnItemClickListener { baseQuickAdapter, view, i ->
                val entity = adapter.getItem(i) ?: return@OnItemClickListener
                adapter.selected = entity.locale
                adapter.notifyDataSetChanged()
            }

        binding.rv.addItemDecoration(
            DataDividerItemDecoration(
                adapter = adapter,
                size = 0.5f,
                dividerColor = 0xffF1F1F1.toInt(),
                horizontalMargin = 26)
        )

        binding.save.setOnSingleClickListener({
            adapter.selected?.let {
                changeLanguage(it)
            }
        }, 500)
    }


    private fun changeLanguage(locale: Locale) {
        context?.let {
            LanguageUtils.applyLanguage(locale)
            val intent = Intent(this.context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            DRouter.build("/main")
                .putExtra(Extend.START_ACTIVITY_VIA_INTENT, intent)
                .start()
            (context as? AppCompatActivity?)?.finish()
        }

    }

}