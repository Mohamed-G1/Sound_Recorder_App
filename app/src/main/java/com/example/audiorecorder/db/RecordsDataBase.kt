package com.example.audiorecorder.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import javax.inject.Inject

@Database(entities = [AudioRecord::class], version = 1, exportSchema = false)
abstract class RecordsDataBase : RoomDatabase() {
    abstract fun audioRecordDao(): AudioRecordDao





//    companion object {
//
//        @Volatile
//        //volatile, meaning that writes to this field are immediately made visible to other threads.
//        private var instance: RecordsDataBase? = null
//        private val LOCK = Any()
//
//        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
//            instance ?: createDatabase(context).also {
//                instance
//            }
//        }
//
//        private fun createDatabase(context: Context) {
//            // insert db
//            Room.databaseBuilder(
//                context.applicationContext,
//                RecordsDataBase::class.java, "audioRecords"
//            ).allowMainThreadQueries()
//                .build()
//        }
//    }
}