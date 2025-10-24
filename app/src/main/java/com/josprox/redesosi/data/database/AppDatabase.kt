package com.josprox.redesosi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.josprox.redesosi.data.InitialData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SubjectEntity::class, ModuleEntity::class, SubmoduleEntity::class, QuestionEntity::class,TestAttemptEntity::class, // <-- AÑADIR
        UserAnswerEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_app_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.studyDao())
                }
            }
        }

        suspend fun populateDatabase(dao: StudyDao) {
            if (dao.getSubjectCount() == 0) {
                dao.insertSubjects(InitialData.subjects)
                dao.insertModules(InitialData.modules)
                dao.insertSubmodules(InitialData.submodules)
            }
        }
    }
}
