package com.josprox.redesosi.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
    )]
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
    )]
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
    )]
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
