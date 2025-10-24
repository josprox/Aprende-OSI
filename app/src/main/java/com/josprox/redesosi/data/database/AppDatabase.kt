package com.josprox.redesosi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.josprox.redesosi.data.InitialData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SubjectEntity::class, ModuleEntity::class, SubmoduleEntity::class, QuestionEntity::class,TestAttemptEntity::class,
        UserAnswerEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // --- AÑADIMOS LA MIGRACIÓN ---
        /**
         * Migración de la versión 2 a 3.
         * Añade las columnas 'author' y 'version' a la tabla 'subjects'.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Añade las nuevas columnas a la tabla 'subjects'
                // Les damos un valor por defecto para los registros existentes
                db.execSQL("ALTER TABLE subjects ADD COLUMN author TEXT NOT NULL DEFAULT 'Desconocido'")
                db.execSQL("ALTER TABLE subjects ADD COLUMN version TEXT NOT NULL DEFAULT '1.0'")
            }
        }
        // --- FIN DE LA MIGRACIÓN ---

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_app_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .addMigrations(MIGRATION_2_3) // <-- APLICAMOS LA MIGRACIÓN
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
            // El 'populate' solo se ejecuta la primera vez que se crea la BD
            // Usará los datos de InitialData, que ya incluyen autor y versión
            if (dao.getSubjectCount() == 0) {
                dao.insertSubjects(InitialData.subjects)
                dao.insertModules(InitialData.modules)
                dao.insertSubmodules(InitialData.submodules)
            }
        }
    }
}