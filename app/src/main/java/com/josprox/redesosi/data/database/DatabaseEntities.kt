package com.josprox.redesosi.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index // Importamos Index para definir los índices

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(
    tableName = "modules",
    foreignKeys = [ForeignKey(
        entity = SubjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )],
    // Solución para la advertencia de 'subjectId'
    indices = [Index("subjectId")]
)
data class ModuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val title: String,
    val shortDescription: String
)

@Entity(
    tableName = "submodules",
    foreignKeys = [ForeignKey(
        entity = ModuleEntity::class,
        parentColumns = ["id"],
        childColumns = ["moduleId"],
        onDelete = ForeignKey.CASCADE
    )],
    // Solución para la advertencia de 'moduleId'
    indices = [Index("moduleId")]
)
data class SubmoduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val moduleId: Int,
    val title: String,
    val contentMd: String // Contenido en formato Markdown
)

@Entity(
    tableName = "questions",
    foreignKeys = [ForeignKey(
        entity = ModuleEntity::class,
        parentColumns = ["id"],
        childColumns = ["moduleId"],
        onDelete = ForeignKey.CASCADE
    )],
    // Solución para la advertencia de 'moduleId'
    indices = [Index("moduleId")]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val moduleId: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String // "A", "B", o "C"
)

@Entity(
    tableName = "test_attempts",
    foreignKeys = [ForeignKey(
        entity = ModuleEntity::class,
        parentColumns = ["id"],
        childColumns = ["moduleId"],
        onDelete = ForeignKey.CASCADE
    )],
    // Solución para la advertencia de 'moduleId'
    indices = [Index("moduleId")]
)
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moduleId: Int,
    val status: String, // "PENDING" o "COMPLETED"
    val score: Double = 0.0, // Calificación sobre 10 (ej. 8.5)
    val totalQuestions: Int,
    val correctAnswers: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val currentQuestionIndex: Int = 0
)

@Entity(
    tableName = "user_answers",
    foreignKeys = [
        ForeignKey(
            entity = TestAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["testAttemptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Solución para las advertencias de 'testAttemptId' y 'questionId'
    indices = [
        Index("testAttemptId"),
        Index("questionId")
    ]
)
data class UserAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val testAttemptId: Long,
    val questionId: Int,
    val selectedOption: String, // "A", "B", "C", o "D"
    val isCorrect: Boolean
)
