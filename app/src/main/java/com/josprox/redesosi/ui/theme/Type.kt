// Type.kt

package com.josprox.redesosi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Configuración de tipografía que enfatiza la estructura y legibilidad (Escolar)
val Typography = Typography(
    // Titulares y títulos (Audaces y Estructurales)
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold, // Muy fuerte para secciones
        fontSize = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // Ideal para títulos de tarjetas
        fontSize = 22.sp
    ),

    // Cuerpo y contenido (Claro y legible)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal, // Base para el contenido de estudio
        fontSize = 16.sp,
        lineHeight = 24.sp, // Aumenta el espacio entre líneas para mejor lectura
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),

    // Etiquetas (Botones, pequeñas descripciones)
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // Fuerte pero no dominante
        fontSize = 14.sp
    )
)