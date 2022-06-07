package com.example.audiorecorder.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.audiorecorder.R
import com.example.audiorecorder.adapter.OnItemClickListener
import com.example.audiorecorder.adapter.RecyclerAdapter
import com.example.audiorecorder.databinding.ActivityGalleryBinding
import com.example.audiorecorder.db.AudioRecord
import com.example.audiorecorder.db.RecordsDataBase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GalleryActivity : AppCompatActivity(), OnItemClickListener {


    lateinit var binding: ActivityGalleryBinding
    private lateinit var records: ArrayList<AudioRecord>

    lateinit var mAdapter: RecyclerAdapter
    lateinit var db: RecordsDataBase
    private var isAllChecked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gallery)
        showToolBarInEditMode()
        db = Room.databaseBuilder(
            this,
            RecordsDataBase::class.java,
            "audio_record"
        ).allowMainThreadQueries()
            .build()


        records = ArrayList()

        mAdapter = RecyclerAdapter(records, this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)

        }

        fetchAll()
        handleSearchInput()
        onCloseButtonClicked()
        onCheckAllButtonClicked()
    }

    private fun fetchAll() {
        GlobalScope.launch(Dispatchers.Main) {
            records.clear()
//            mAdapter.differ.submitList(db.audioRecordDao().getAll())
            var query = db.audioRecordDao().getAll()
            records.addAll(query)

            mAdapter.notifyDataSetChanged()
        }
    }

    fun handleSearchInput() {
        GlobalScope.launch {
            binding.searchInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    var query = p0.toString()
                    searchDataBase(query)
                }

                override fun afterTextChanged(p0: Editable?) {}
            })
        }

    }

    fun searchDataBase(query: String) {
        GlobalScope.launch(Dispatchers.Main) {
            records.clear()
            var query = db.audioRecordDao()
                .searchDataBase("%$query%")// this %$query% to get result if searched item contain any charcter i typed
            records.addAll(query)

            runOnUiThread {
                mAdapter.notifyDataSetChanged()
            }

        }
    }

    override fun onItemClickListener(position: Int) {
        val record = records[position]
        // this to dont start the screen with clicked
        // and to check in checkbox instead of start the screen
        if (mAdapter.isEditMode()) {
            mAdapter.setEditMode(true)
            records[position].isChecked = !records[position].isChecked
            mAdapter.notifyItemChanged(position)
        } else {

            var intent = Intent(this, AudioPlayerActivity::class.java)
            intent.putExtra("filepath", record.filePath)
            intent.putExtra("filename", record.fileName)
            startActivity(intent)
        }


    }


    override fun onItemLongClickListener(position: Int) {
        mAdapter.setEditMode(true)
        records[position].isChecked = !records[position].isChecked
        mAdapter.notifyItemChanged(position)

        if (mAdapter.isEditMode() && binding.editBarLayout.visibility == View.GONE) {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)

            binding.editBarLayout.visibility = View.VISIBLE
        }
    }

    // this to setup toolbar in edit mode
    private fun showToolBarInEditMode() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
    }

    // handle on close button in toolbar clicked
    // hide tool bar when clicked
    private fun onCloseButtonClicked() {
        binding.closelButton.setOnClickListener {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            binding.editBarLayout.visibility = View.GONE

            records.map { it.isChecked = false }

            mAdapter.setEditMode(false)
        }
    }

    private fun onCheckAllButtonClicked() {
        binding.selectAlllButton.setOnClickListener {
            isAllChecked = !isAllChecked
            records.map { it.isChecked = isAllChecked }
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (mAdapter.isEditMode())
            mAdapter.setEditMode(false)
    }
}