package com.josprox.redesosi.data

import kotlinx.serialization.Serializable

/**
 * Este archivo define la estructura que el JSON del usuario debe tener.
 * Es una estructura anidada para que sea fácil de escribir.
 * NÓTESE LA AUSENCIA DE IDs.
 */

@Serializable
data class SubmoduleImport(
    val title: String,
    val contentMd: String
)

@Serializable
data class ModuleImport(
    val title: String,
    val shortDescription: String,
    val submodules: List<SubmoduleImport>
)

@Serializable
data class SubjectImport(
    val name: String,
    val modules: List<ModuleImport>
)