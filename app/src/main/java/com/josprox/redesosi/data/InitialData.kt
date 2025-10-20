package com.josprox.redesosi.data

import com.josprox.redesosi.data.database.ModuleEntity
import com.josprox.redesosi.data.database.SubjectEntity
import com.josprox.redesosi.data.database.SubmoduleEntity

/**
 * Proporciona los datos iniciales (semilla) para la base de datos de la aplicación.
 * Contiene la estructura completa de las materias, módulos y submódulos.
 */
object InitialData {

    /**
     * Lista de materias principales.
     */
    val subjects = listOf(
        SubjectEntity(id = 1, name = "Redes de Computadoras")
    )

    /**
     * Lista de módulos de estudio, agrupados por materia.
     */
    val modules = listOf(
        ModuleEntity(id = 1, subjectId = 1, title = "Modelo OSI", shortDescription = "Comprende el modelo de referencia de 7 capas."),
        ModuleEntity(id = 2, subjectId = 1, title = "Modelo TCP/IP", shortDescription = "Explora el modelo práctico que utiliza Internet.")
    )

    /**
     * Lista completa de submódulos (capas y contenido), agrupados por módulo.
     */
    val submodules = listOf(
        // === MÓDULO 1: MODELO OSI ===

        SubmoduleEntity(id = 1, moduleId = 1, title = "Capa 1: Física", contentMd = """
            ### Descripción
            La capa Física es la más baja del modelo OSI. Se encarga de la transmisión y recepción de un flujo no estructurado de bits sin procesar a través de un medio físico. Define las especificaciones eléctricas, mecánicas, de procedimiento y funcionales para activar, mantener y desactivar el enlace físico.
            
            ### Funciones Principales
            - **Definición de características físicas:** Tipo de conector (RJ45, fibra), asignación de pines.
            - **Señalización y codificación:** Cómo se convierte un '1' o '0' en una señal eléctrica, de luz o de radio.
            - **Tasa de bits (Bit Rate):** Define la velocidad de transmisión (ej. 100 Mbps, 1 Gbps).
            - **Topología de red:** Define cómo están dispuestos físicamente los dispositivos (bus, estrella, anillo).
            
            ### Ejemplos de hardware y protocolos
            - Cables (Ethernet Cat 6, Fibra óptica, Coaxial)
            - Hubs (Concentradores)
            - Repetidores
            - Tarjetas de red (NIC) a nivel de hardware
            - Bluetooth (capa física)
        """.trimIndent()),

        SubmoduleEntity(id = 2, moduleId = 1, title = "Capa 2: Enlace de Datos", contentMd = """
            ### Descripción
            Esta capa proporciona un tránsito de datos fiable a través de un enlace físico. Se ocupa del direccionamiento físico (dirección MAC), el control de errores a nivel de enlace y el control de flujo. Se divide en dos subcapas:
            1.  **MAC (Media Access Control):** Controla cómo los dispositivos acceden al medio y el permiso para transmitir.
            2.  **LLC (Logical Link Control):** Identifica y encapsula los protocolos de la capa de red.
            
            ### Funciones Principales
            - **Tramas (Framing):** Encapsula los paquetes de la Capa 3 en tramas (frames), añadiendo cabeceras y colas.
            - **Direccionamiento Físico:** Utiliza direcciones MAC para identificar dispositivos en la red local.
            - **Control de Errores:** Detecta (y a veces corrige) errores que pueden haber ocurrido en la capa física.
            - **Control de Flujo:** Evita que un transmisor rápido sature a un receptor lento.
            
            ### Ejemplos de hardware y protocolos
            - Switch (Conmutador)
            - Bridge (Puente)
            - Tarjetas de red (NIC)
            - Ethernet
            - Wi-Fi (802.11)
            - ARP (Address Resolution Protocol)
        """.trimIndent()),

        SubmoduleEntity(id = 3, moduleId = 1, title = "Capa 3: Red", contentMd = """
            ### Descripción
            La capa de Red es responsable del reenvío de paquetes, incluyendo el enrutamiento a través de diferentes redes. Es el "GPS" de Internet. Gestiona el direccionamiento lógico (direcciones IP) para encontrar la mejor ruta hacia el destino.
            
            ### Funciones Principales
            - **Enrutamiento (Routing):** Determina la mejor ruta para enviar paquetes desde el origen al destino a través de múltiples redes.
            - **Direccionamiento Lógico:** Asigna y gestiona direcciones lógicas (IP) para identificar hosts en la red.
            - **Conmutación de paquetes:** Divide los mensajes en paquetes más pequeños para su transmisión.
            
            ### Ejemplos de hardware y protocolos
            - Router (Enrutador)
            - Switch de Capa 3
            - IP (Internet Protocol v4 y v6)
            - ICMP (Internet Control Message Protocol - usado por `ping`)
            - OSPF, BGP (Protocolos de enrutamiento)
        """.trimIndent()),

        SubmoduleEntity(id = 4, moduleId = 1, title = "Capa 4: Transporte", contentMd = """
            ### Descripción
            Proporciona servicios de transferencia de datos de extremo a extremo, fiables o no fiables. Es el "servicio de mensajería" que se asegura de que el mensaje completo llegue, o simplemente lo envía sin confirmación.
            
            ### Funciones Principales
            - **Segmentación y Reensamblaje:** Divide los datos de la capa de sesión en segmentos y los vuelve a ensamblar en el destino.
            - **Control de Conexión:** Ofrece servicios orientados a conexión (TCP) y no orientados a conexión (UDP).
            - **Fiabilidad (con TCP):** Asegura la entrega de datos sin errores, en secuencia y sin duplicados.
            - **Control de Flujo y Congestión:** Gestiona la velocidad de transmisión para no saturar la red o el receptor.
            - **Multiplexación:** Permite que múltiples aplicaciones en un host usen la red simultáneamente (mediante puertos).
            
            ### Protocolos
            - **TCP (Transmission Control Protocol):** Fiable, orientado a la conexión (ej. navegación web, email).
            - **UDP (User Datagram Protocol):** Rápido, no fiable, sin conexión (ej. streaming, juegos online, DNS).
        """.trimIndent()),

        SubmoduleEntity(id = 5, moduleId = 1, title = "Capa 5: Sesión", contentMd = """
            ### Descripción
            Establece, gestiona y finaliza las sesiones (diálogos) entre dos aplicaciones. Es el "moderador" de la conversación.
            
            ### Funciones Principales
            - **Establecimiento de Sesión:** Inicia, mantiene y cierra una conexión (diálogo) entre aplicaciones.
            - **Control de Diálogo:** Determina quién puede transmitir y cuándo (simplex, half-duplex, full-duplex).
            - **Sincronización:** Inserta "puntos de control" (checkpoints) en el flujo de datos. Si la sesión falla, puede reanudarse desde el último punto de control, en lugar de empezar de cero.
            
            ### Ejemplos de protocolos
            - RPC (Remote Procedure Call)
            - NetBIOS (Network Basic Input/Output System)
            - PPTP (Point-to-Point Tunneling Protocol)
        """.trimIndent()),

        SubmoduleEntity(id = 6, moduleId = 1, title = "Capa 6: Presentación", contentMd = """
            ### Descripción
            Es el "traductor universal" del modelo. Se asegura de que la información enviada por la capa de aplicación de un sistema pueda ser leída por la capa de aplicación de otro.
            
            ### Funciones Principales
            - **Traducción de Datos:** Convierte los datos entre diferentes formatos (ej. de ASCII a EBCDIC).
            - **Cifrado y Descifrado:** Se encarga de cifrar los datos en el origen y descifrarlos en el destino para mantener la privacidad (ej. SSL/TLS).
            - **Compresión:** Comprime los datos para reducir el ancho de banda necesario para la transmisión.
            
            ### Ejemplos de protocolos y estándares
            - **Cifrado:** SSL (Secure Sockets Layer), TLS (Transport Layer Security)
            - **Formatos de imagen:** JPEG, GIF, PNG
            - **Formatos de video:** MPEG
            - **Codificación:** ASCII, EBCDIC
        """.trimIndent()),

        SubmoduleEntity(id = 7, moduleId = 1, title = "Capa 7: Aplicación", contentMd = """
            ### Descripción
            La capa de Aplicación es la más cercana al usuario. Proporciona servicios de red directamente a las aplicaciones del usuario final. Es la interfaz que vemos (navegador web, cliente de correo).
            
            ### Funciones Principales
            - **Identificación de interlocutores:** Asegura que el destinatario esté disponible y pueda ser contactado.
            - **Proporcionar servicios de red:** Ofrece los protocolos que las aplicaciones usan para funcionar.
            - **Sincronización de la comunicación.**
            
            ### Ejemplos de protocolos
            - **HTTP/HTTPS:** Navegación web
            - **FTP/SFTP:** Transferencia de archivos
            - **SMTP:** Envío de correo electrónico
            - **POP3/IMAP:** Recepción de correo electrónico
            - **DNS (Domain Name System):** Traducción de nombres de dominio a IP
            - **Telnet/SSH:** Acceso remoto
        """.trimIndent()),

        // === MÓDULO 2: MODELO TCP/IP ===

        SubmoduleEntity(id = 8, moduleId = 2, title = "Capa 1: Acceso a la Red", contentMd = """
            ### Descripción
            Esta capa (también llamada "Capa de Enlace" o "Capa de Host a Red") es la más baja del modelo TCP/IP. Combina las funciones de las capas Física (Capa 1) y de Enlace de Datos (Capa 2) del modelo OSI.
            
            Se ocupa de todos los componentes de hardware y software implicados en el enlace físico y la transmisión de tramas.
            
            ### Funciones Principales
            - **Interfaz con el hardware:** Define cómo transmitir datos sobre el medio físico (cable, aire).
            - **Tramas (Framing):** Encapsula datagramas IP en tramas para su transmisión.
            - **Direccionamiento Físico (MAC):** Gestiona las direcciones MAC.
            - **Resolución de direcciones:** El protocolo ARP (Address Resolution Protocol) opera aquí para mapear direcciones IP a direcciones MAC.
            
            ### Protocolos y estándares
            - Ethernet
            - Wi-Fi (802.11)
            - ARP
            - Controladores de dispositivo (Drivers)
        """.trimIndent()),

        SubmoduleEntity(id = 9, moduleId = 2, title = "Capa 2: Internet", contentMd = """
            ### Descripción
            Esta capa es paralela a la capa de Red (Capa 3) del modelo OSI. Su función principal es organizar y entregar los datos en paquetes (datagramas) desde el origen al destino a través de múltiples redes.
            
            Es el núcleo de la interconexión de redes (Internet).
            
            ### Funciones Principales
            - **Enrutamiento:** Determina la mejor ruta para los paquetes.
            - **Direccionamiento Lógico:** Utiliza el Protocolo de Internet (IP) para asignar direcciones únicas a los hosts.
            - **Fragmentación de paquetes:** Divide paquetes grandes si es necesario para que quepan en las tramas de la capa inferior.
            
            ### Protocolos Principales
            - **IP (Internet Protocol v4 y v6):** El protocolo fundamental para el direccionamiento y enrutamiento.
            - **ICMP (Internet Control Message Protocol):** Para diagnóstico y reporte de errores (usado por `ping` y `traceroute`).
            - **IPsec (Internet Protocol Security):** Para comunicaciones IP seguras.
        """.trimIndent()),

        SubmoduleEntity(id = 10, moduleId = 2, title = "Capa 3: Transporte", contentMd = """
            ### Descripción
            Esta capa es funcionalmente idéntica a la capa de Transporte (Capa 4) del modelo OSI. Proporciona una conexión lógica de extremo a extremo entre las aplicaciones del host de origen y destino.
            
            ### Funciones Principales
            - **Segmentación:** Divide los datos de la aplicación en segmentos (TCP) o datagramas (UDP).
            - **Multiplexación:** Utiliza números de puerto (ej. Puerto 80 para HTTP) para dirigir los datos a la aplicación correcta en el host de destino.
            - **Fiabilidad (TCP):** Proporciona un servicio fiable, orientado a conexión, con control de errores y de flujo.
            - **Servicio no fiable (UDP):** Proporciona un servicio rápido, sin conexión, pero no garantiza la entrega.
            
            ### Protocolos Principales
            - **TCP (Transmission Control Protocol)**
            - **UDP (User Datagram Protocol)**
        """.trimIndent()),

        SubmoduleEntity(id = 11, moduleId = 2, title = "Capa 4: Aplicación", contentMd = """
            ### Descripción
            La capa de Aplicación en TCP/IP combina las funciones de las capas de Sesión, Presentación y Aplicación (Capas 5, 6 y 7) del modelo OSI.
            
            Contiene todos los protocolos de alto nivel que las aplicaciones utilizan para comunicarse e intercambiar datos.
            
            ### Funciones Principales
            - **Comunicación entre procesos:** Permite que las aplicaciones en diferentes hosts se comuniquen.
            - **Representación de datos:** Los protocolos de esta capa definen el formato, la codificación y el cifrado de los datos (ej. HTTPS lo hace aquí).
            - **Gestión de sesión:** Los propios protocolos (como HTTP) gestionan el estado de la sesión.
            
            ### Protocolos Principales
            - **HTTP/HTTPS:** Navegación web
            - **FTP/SFTP:** Transferencia de archivos
            - **SMTP, POP3, IMAP:** Correo electrónico
            - **DNS:** Resolución de nombres
            - **SSH:** Acceso remoto seguro
            - **DHCP:** Configuración automática de IP
        """.trimIndent())
    )
}