package com.example.audiorecorder.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.audiorecorder.R
import com.example.audiorecorder.db.AudioRecord
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerAdapter(val records: ArrayList<AudioRecord>, val listener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    // this to show edit mode whrn long press
    private var editMode = false

    fun isEditMode(): Boolean {
        return editMode
    }

    fun setEditMode(mode: Boolean) {
        if (editMode != mode) {
            editMode = true
            notifyDataSetChanged()
        }
    }


    //------------------------------------------ Normal Adapter
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {
        var tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
        var tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
        var checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        // this init ti activate
        // at runtime android knows which one to pass here
        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION)
                listener.onItemClickListener(position)
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION)
                listener.onItemLongClickListener(position)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return records.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // this bez trying to draw items while recycler view is still loading
        if (position != RecyclerView.NO_POSITION) {
            var record = records[position]

            // time format
            var simpleFormat = SimpleDateFormat("dd/MM/yyyy")
            var date = Date(record.timesTamp)
            var strDate = simpleFormat.format(date)


            holder.tvFileName.text = record.fileName
            holder.tvMeta.text = "${record.duration} $strDate"

            // to show check box when long pressed
            if (editMode) {
                holder.checkBox.visibility = View.VISIBLE
                holder.checkBox.isChecked = record.isChecked
            } else {
                holder.checkBox.visibility = View.GONE
                holder.checkBox.isChecked = false
            }
        }
    }


    //--------------------------------------------DiffUtil test
//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
//        View.OnClickListener, View.OnLongClickListener {
//        var tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
//        var tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
//        var checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
//
//
//        // this init ti activate
//        // at runtime android knows which one to pass here
//        init {
//            itemView.setOnClickListener(this)
//            itemView.setOnLongClickListener(this)
//        }
//
//        override fun onClick(onclick: View?) {
//            val position = adapterPosition
//            if (position != RecyclerView.NO_POSITION)
//                listener.onItemClickListener(position)
//        }
//
//        override fun onLongClick(longClick: View?): Boolean {
//            val position = adapterPosition
//            if (position != RecyclerView.NO_POSITION)
//                listener.onItemLongClickListener(position)
//            return true
//        }
//    }
//
//
//    private val differCallBack = object : DiffUtil.ItemCallback<AudioRecord>() {
//        override fun areItemsTheSame(oldItem: AudioRecord, newItem: AudioRecord): Boolean {
//            return oldItem.fileName == newItem.fileName
//        }
//
//        override fun areContentsTheSame(oldItem: AudioRecord, newItem: AudioRecord): Boolean {
//            return oldItem == newItem
//        }
//    }
//
//    val differ = AsyncListDiffer(this, differCallBack)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//
//        var record = differ.currentList[position]
//
//        // time format
//        var simpleFormat = SimpleDateFormat("dd/MM/yyyy")
//        var date = Date(record.timesTamp)
//        var strDate = simpleFormat.format(date)
//
//        holder.tvFileName.text = record.fileName
//        holder.tvMeta.text = "${record.duration} $strDate"
//    }
//
//    override fun getItemCount(): Int {
//        return differ.currentList.size
//    }

}