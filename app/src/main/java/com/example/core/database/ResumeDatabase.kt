package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ResumeEntity::class, TemplateCacheEntity::class, SkillEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ResumeDatabase : RoomDatabase() {

    abstract fun resumeDao(): ResumeDao
    abstract fun templateCacheDao(): TemplateCacheDao
    abstract fun skillDao(): SkillDao

    companion object {
        @Volatile
        private var INSTANCE: ResumeDatabase? = null

        fun getInstance(context: Context): ResumeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ResumeDatabase::class.java,
                    "resumecraft_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
