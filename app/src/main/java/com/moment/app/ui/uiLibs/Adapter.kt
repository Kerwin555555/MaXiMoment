package com.moment.app.ui.uiLibs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnPreDrawListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.moment.app.databinding.RecyclerItemViewBinding

open class DAdapter(val context: Context): RecyclerView.Adapter<DAdapter.VH>(){

    private var mHasRecorded = false

    class VH(val binding: RecyclerItemViewBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(RecyclerItemViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return 200
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (!mHasRecorded && position == 0) {
            mHasRecorded = true
            holder.binding.root.viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    holder.binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            })
        }
        holder.binding.root.text = "Hi." + position
        holder.binding.root.setOnClickListener {

        }
    }

    var runnable: Runnable? = null
}