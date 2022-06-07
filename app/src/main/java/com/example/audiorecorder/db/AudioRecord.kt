package com.example.audiorecorder.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "audioRecords")
data class AudioRecord(
    var fileName: String,
    var filePath: String,
    var timesTamp: Long,
    var duration: String,
    var ampsPath: String
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    @Ignore
    var isChecked = false
}

