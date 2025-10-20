package com.josprox.redesosi.data

import com.josprox.redesosi.data.database.ModuleEntity
import com.josprox.redesosi.data.database.SubjectEntity
import com.josprox.redesosi.data.database.SubmoduleEntity

object InitialData {
    val subjects = listOf(
        SubjectEntity(id = 1, name = "Redes de Computadoras")
    )

    val modules = listOf(
        ModuleEntity(id = 1, subjectId = 1, title = "Modelo OSI", shortDescription = "Comprende el modelo de referencia de 7 capas."),
        ModuleEntity(id = 2, subjectId = 1, title = "Modelo TCP/IP", shortDescription = "Explora el modelo práctico que utiliza Internet.")
    )

    val submodules = listOf(
        // Modelo OSI
        SubmoduleEntity(id = 1, moduleId = 1, title = "Capa 1: Física", contentMd = """
            ### Descripción
            La capa Física es la más baja del modelo OSI. Se encarga de la transmisión y recepción de un flujo no estructurado de bits sin procesar a través de un medio físico. 
            
            ### Funciones Principales
            - **Definición de características eléctricas y mecánicas:** voltajes, temporización, etc.
            - **Transmisión de bits:** convierte bits en señales (eléctricas, ópticas, de radio).
            - **Topología de red:** define cómo están dispuestos los dispositivos.
            
            ### Ejemplos de hardware
            - Cables (Ethernet, fibra óptica)
            - Hubs
            - Repetidores
        """.trimIndent()),
        SubmoduleEntity(id = 2, moduleId = 1, title = "Capa 7: Aplicación", contentMd = """
            ### Descripción
            La capa de Aplicación es la más cercana al usuario. Proporciona servicios de red a las aplicaciones del usuario final.
            
            ### Funciones Principales
            - **Identificación de interlocutores:** asegura que el destinatario esté disponible.
            - **Sincronización:** gestiona la comunicación entre aplicaciones.
            - **Protocolos de alto nivel:** HTTP, FTP, SMTP, DNS.
            
            ### Ejemplos de uso
            - Navegadores web
            - Clientes de correo electrónico
            - Servidores de archivos
        """.trimIndent()),
        // Modelo TCP/IP
        SubmoduleEntity(id = 3, moduleId = 2, title = "Capa de Acceso a la Red", contentMd = """
            ### Descripción
            Esta capa combina las funciones de las capas Física y de Enlace de Datos del modelo OSI. Se ocupa de todos los componentes hardware y software implicados en el enlace físico.
            
            ### Funciones Principales
            - **Interfaz con el hardware de red:** tarjetas de red.
            - **Formateo de datos:** encapsulación de datagramas IP en tramas.
            - **Control de acceso al medio:** MAC.
            
            ### Protocolos
            - Ethernet
            - Wi-Fi
            - ARP
        """.trimIndent()),
        SubmoduleEntity(id = 4, moduleId = 2, title = "Capa de Aplicación", contentMd = """
            ### Descripción
            La capa de Aplicación del modelo TCP/IP combina las capas de Sesión, Presentación y Aplicación del modelo OSI. Define los protocolos que las aplicaciones utilizan para intercambiar datos.
            
            ### Funciones Principales
            - **Comunicación entre procesos:** permite que las aplicaciones en diferentes hosts se comuniquen.
            - **Representación de datos:** se asume que las aplicaciones manejan su propia sintaxis de datos.
            
            ### Protocolos
            - HTTP (Hypertext Transfer Protocol)
            - FTP (File Transfer Protocol)
            - SMTP (Simple Mail Transfer Protocol)
            - DNS (Domain Name System)
        """.trimIndent())
    )
}
