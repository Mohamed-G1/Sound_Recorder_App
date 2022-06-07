package com.example.audiorecorder.adapter

interface OnItemClickListener {

    fun onItemClickListener(position: Int)  // this position to know weich item will clicked
    fun onItemLongClickListener(position: Int) // this position to know weich item will clicked
}