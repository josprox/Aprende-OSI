package com.josprox.redesosi.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalInfoScreen(navController: NavHostController) {
    val uriHandler = LocalUriHandler.current // Herramienta para abrir enlaces externos

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Información Legal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Título Principal
            Text(
                "Acuerdos y Políticas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            // --- Sección 1: Términos y Condiciones ---
            LegalSection(title = "Términos y Condiciones de Uso (T&C)") {
                Text(
                    text = "Al usar esta aplicación, usted acepta los siguientes términos: La aplicación es proporcionada 'tal cual' sin garantías. El desarrollador no se hace responsable por la pérdida de datos o daños derivados de su uso. El contenido está protegido por derechos de autor.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // --- Sección 2: Licencia y Código Fuente ---
            LegalSection(title = "Licencia de Código Fuente (Source-Available)") {
                Text(
                    text = "El código fuente de esta aplicación está disponible para su consulta y auditoría pública. Sin embargo, este software NO es de código abierto (Open Source).",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Derechos del Usuario:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Usted tiene derecho a **consultar** y **aportar** (mediante pull requests o reportes) al código fuente.\n\n" +
                            "Restricciones:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Está **estrictamente prohibido modificar, distribuir, republicar, sublicenciar o utilizar el código para fines comerciales o derivados** sin el permiso explícito y escrito del desarrollador.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // --- Sección 3: Política de Privacidad ---
            LegalSection(title = "Política de Privacidad") {
                Text(
                    text = "Esta aplicación está diseñada para ser privada y no recopila información personal identificable. Los datos (materias, notas) se almacenan localmente en su dispositivo y nunca se transmiten a servidores externos sin su consentimiento explícito (como al crear un backup local).",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // --- Sección 4: Componentes de Terceros ---
            LegalSection(title = "Componentes de Terceros (Código Abierto)") {
                Text(
                    text = "Esta aplicación utiliza bibliotecas de terceros que sí están licenciadas bajo licencias de Código Abierto (Open Source), incluyendo AndroidX, Jetpack Compose y Kotlin. Se respetan todas las obligaciones de licencia de estos componentes.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // --- Sección 5: Soporte y Contacto (NUEVA) ---
            LegalSection(title = "Soporte y Autoría") {
                Text(
                    text = "Aplicación creada por:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Melchor Estrada José Luis - JOSPROX MX",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Para soporte, reportes de errores, dudas o cualquier otra consulta, por favor utilice el siguiente enlace:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Enlace Clickable
                val annotatedLinkString = buildAnnotatedString {
                    append("Enlace de Soporte: ")
                    pushStringAnnotation(tag = "URL", annotation = "https://josprox.com/soporte/")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                        append("josprox.com/soporte/")
                    }
                    pop()
                }

                ClickableText(
                    text = annotatedLinkString,
                    onClick = { offset ->
                        annotatedLinkString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                uriHandler.openUri(annotation.item)
                            }
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pie de página (ejemplo)
            Text(
                "JOSPROX MX",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun LegalSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}