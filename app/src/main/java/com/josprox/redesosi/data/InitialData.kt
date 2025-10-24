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
        ModuleEntity(id = 2, subjectId = 1, title = "Modelo TCP/IP", shortDescription = "Explora el modelo práctico que utiliza Internet."),
        ModuleEntity(id = 3, subjectId = 1, title = "Conceptos de WAN", shortDescription = "Fundamentos de redes de área amplia y tecnologías."),
        ModuleEntity(id = 4, subjectId = 1, title = "Conexiones Punto a Punto", shortDescription = "Protocolos HDLC y PPP para enlaces seriales."),
        ModuleEntity(id = 5, subjectId = 1, title = "Conexiones de Acceso Remoto", shortDescription = "PPPoE, VPN y tecnologías de banda ancha.")
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
        """.trimIndent()),

        // === MÓDULO 3: CONCEPTOS DE WAN ===

        SubmoduleEntity(id = 12, moduleId = 3, title = "Introducción a las WAN", contentMd = """
            ### ¿Qué es una WAN?
            Una Red de Área Amplia (WAN) interconecta usuarios y LANs más allá del ámbito geográfico de una LAN local. Mientras que una LAN suele ser propiedad de una organización, una WAN es propiedad de un proveedor de servicios.

            ### Propósito de las WAN
            - Interconectar LANs empresariales con sucursales remotas
            - Conectar empleados en teletrabajo
            - Permitir comunicación con clientes y proveedores
            - Compartir información a través de grandes distancias

            ### Topologías WAN Comunes
            - **Punto a Punto:** Conexión directa entre dos sitios
            - **Estrella (Hub and Spoke):** Sitios radiales conectados a un concentrador central
            - **Malla Completa:** Cada sitio conectado directamente con todos los demás
            - **Doble Conexión:** Redundancia mediante conexiones múltiples

            ### Evolución de las Redes
            Las necesidades WAN evolucionan con el crecimiento empresarial:
            - **Oficina Pequeña:** DSL y LAN única
            - **Campus Empresarial:** Múltiples subredes y personal TI interno
            - **Red de Sucursales:** WAN para conectar ubicaciones remotas
            - **Empresa Global:** Conectividad mundial con reducción de costos
        """.trimIndent()),

        SubmoduleEntity(id = 13, moduleId = 3, title = "Terminología y Dispositivos WAN", contentMd = """
            ### Terminología Común
            - **CPE (Customer Premises Equipment):** Equipo en el perímetro empresarial
            - **DCE (Data Communications Equipment):** Dispositivos que conectan al bucle local
            - **DTE (Data Terminal Equipment):** Dispositivos del cliente que transmiten datos
            - **Punto de Demarcación:** Separación entre equipo del cliente y proveedor
            - **Bucle Local ("Última Milla"):** Conexión física al proveedor
            - **CO (Central Office):** Instalación del proveedor local

            ### Dispositivos WAN
            - **Módems:** Conversión señales digitales/analógicas
            - **CSU/DSU:** Terminación de líneas digitales
            - **Routers:** Internetworking y puertos WAN
            - **Switches WAN:** Conmutación en el backbone
            - **Servidores de Acceso:** Control de comunicaciones dial-up

            ### Operaciones WAN en el Modelo OSI
            Las WAN operan principalmente en las Capas 1 y 2:
            - **Capa Física:** Conexiones eléctricas y medios
            - **Capa de Enlace:** HDLC, PPP, Frame Relay, Ethernet WAN
        """.trimIndent()),

        SubmoduleEntity(id = 14, moduleId = 3, title = "Tecnologías de Conmutación", contentMd = """
            ### Conmutación de Circuitos
            Establece un circuito dedicado antes de la comunicación:
            - **Características:**
              - Conexión dedicada durante la sesión
              - Ancho de banda garantizado
              - Mayor costo por tiempo de conexión
            - **Ejemplos:** PSTN, ISDN

            ### Conmutación de Paquetes
            Divide datos en paquetes que se enrutan por red compartida:
            - **Características:**
              - Uso eficiente del ancho de banda
              - Múltiples comunicaciones simultáneas
              - Menor costo, mayor latencia
            - **Enfoques:**
              - Sin conexión (Internet - direccionamiento en cada paquete)
              - Orientado a conexión (Frame Relay - identificadores)

            ### Comparación
            | Aspecto | Conmutación de Circuitos | Conmutación de Paquetes |
            |---------|--------------------------|-------------------------|
            | Costo | Mayor | Menor |
            | Eficiencia | Baja | Alta |
            | Latencia | Predictible | Variable |
            | Uso Ancho de Banda | Dedicado | Compartido |
        """.trimIndent()),

        SubmoduleEntity(id = 15, moduleId = 3, title = "Infraestructuras WAN", contentMd = """
            ### WAN Privadas
            **Líneas Arrendadas:**
            - Conexiones punto a punto dedicadas
            - T1/E1 (1.544/2.048 Mbps), T3/E3 (45/34 Mbps)
            - Costoso pero confiable

            **Frame Relay:**
            - Tecnología NBMA de capa 2
            - PVC identificados por DLCI
            - Velocidades hasta 4+ Mbps

            **ATM:**
            - Celdas de 53 bytes (5 bytes cabecera)
            - Adecuado para voz y video
            - Mayor sobrecarga que Frame Relay

            **WAN Ethernet:**
            - Estándares de fibra óptica (hasta 70 km)
            - Integración fácil con redes existentes
            - Reemplaza ATM y Frame Relay

            **MPLS:**
            - Switching por etiquetas multiprotocolo
            - Transporta IPv4, IPv6, Ethernet, ATM
            - Alta flexibilidad tecnológica

            **VSAT:**
            - Comunicaciones satelitales
            - Para ubicaciones remotas sin proveedores
            - Latencia alta (35,786 km ida y vuelta)
        """.trimIndent()),

        SubmoduleEntity(id = 16, moduleId = 3, title = "Tecnologías de Banda Ancha Pública", contentMd = """
            ### DSL (Línea de Suscripción Digital)
            - Usa líneas telefónicas existentes
            - **ADSL:** Asimétrico (más descarga que subida)
            - **SDSL:** Simétrico (misma capacidad ambas direcciones)
            - Distancia máxima: 5.46 km del DSLAM

            ### Cable
            - Usa redes de televisión por cable
            - Sistema HFC (Híbrido Fibra-Coaxial)
            - Componentes: CMTS (proveedor) y Cable Módem (cliente)
            - Ancho de banda compartido entre usuarios

            ### Tecnologías Inalámbricas
            **Wi-Fi Municipal:** Malla de puntos de acceso
            **Datos Móviles (3G/4G/LTE):**
            - LTE Cat 10: 450 Mbps descarga, 100 Mbps subida
            - Cobertura celular amplia

            **Internet Satelital:**
            - Para áreas sin acceso terrestre
            - Antena requiere vista despejada al ecuador
            - Capacidad limitada por suscriptor

            **WiMAX (802.16):**
            - Cobertura similar a celular (30 millas)
            - Reemplazado por LTE para acceso móvil
        """.trimIndent()),

        // === MÓDULO 4: CONEXIONES PUNTO A PUNTO ===

        SubmoduleEntity(id = 17, moduleId = 4, title = "Comunicaciones Seriales", contentMd = """
            ### Conceptos Básicos
            Las conexiones punto a punto conectan LANs a WANs de proveedores y también segmentos de LAN. También se denominan "conexiones seriales" o "líneas arrendadas".

            ### Comunicación Serial vs Paralela
            | Característica | Serial | Paralela |
            |----------------|--------|----------|
            | Transmisión | Bits secuenciales por un canal | Múltiples bits simultáneos |
            | Velocidad | Más lenta por bit | Más rápida (1 byte en tiempo de 1 bit serial) |
            | Distancia | Larga distancia | Corta distancia |
            | Cableado | Simple (menos cables) | Complejo (más cables) |

            ### Enlaces Punto a Punto
            - **Uso:** Conexiones dedicadas permanentes
            - **Ventaja:** Ruta preestablecida y dedicada
            - **Aplicaciones:** VoIP, Video sobre IP (requieren disponibilidad constante)
            - **Costo:** Generalmente más costoso que servicios compartidos

            ### Ancho de Banda Serial
            **Especificaciones Norteamericanas (T-carrier):**
            - DS0: 64 Kbps (unidad básica)
            - T1/DS1: 24 DS0 = 1.544 Mbps
            - T3/DS3: 28 T1 = 44.736 Mbps

            **Especificaciones Europeas (E-carrier):**
            - E1: 2.048 Mbps
            - E3: 34.368 Mbps

            **OC (Optical Carrier):**
            - OC-1: 51.84 Mbps
            - OC-3: 155.52 Mbps
            - OC-12: 622.08 Mbps
            - OC-192: 9.954 Gbps
        """.trimIndent()),

        SubmoduleEntity(id = 18, moduleId = 4, title = "Encapsulación HDLC", contentMd = """
            ### ¿Qué es HDLC?
            HDLC (High-Level Data Link Control) es un protocolo sincrónico de capa de enlace de datos desarrollado por ISO. Define una estructura de trama que permite control de flujo y errores.

            ### HDLC Estándar vs HDLC Cisco
            **HDLC Estándar:**
            ```
            [Flag][Address][Control][Data][FCS][Flag]
            ```
            - Solo soporta entornos de protocolo único

            **HDLC Cisco:**
            ```
            [Flag][Address][Control][Protocol][Data][FCS][Flag]
            ```
            - Campo "Protocolo" agregado para soporte multiprotocolo
            - Solo funciona entre dispositivos Cisco

            ### Configuración HDLC
            ```bash
            Router(config)# interface s0/0/0
            Router(config-if)# encapsulation hdlc
            ```
            - HDLC es la encapsulación predeterminada en interfaces seriales Cisco
            - Para conectar con dispositivos no-Cisco, usar PPP

            ### Solución de Problemas
            Comandos útiles:
            - `show interfaces serial x/x/x` - Estado y configuración
            - `show controllers serial x/x/x` - Estado físico y cables

            Estados comunes:
            - `Serial x is up, line protocol is up` - Normal
            - `Serial x is down, line protocol is down` - Problema físico
            - `Serial x is up, line protocol is down` - Problema de configuración
        """.trimIndent()),

        SubmoduleEntity(id = 19, moduleId = 4, title = "Protocolo PPP", contentMd = """
            ### Introducción a PPP
            PPP (Point-to-Point Protocol) se utiliza cuando es necesario conectar un router Cisco a un router que no es de Cisco. PPP encapsula tramas para transmisión a través de enlaces físicos.

            ### Componentes Principales
            1. **Entramado estilo HDLC:** Transporte multiprotocolo
            2. **LCP (Link Control Protocol):** Establecimiento, configuración y prueba de enlace
            3. **NCP (Network Control Protocols):** Configuración de protocolos de capa de red

            ### Ventajas sobre HDLC
            - **Autenticación:** PAP y CHAP
            - **Calidad de Enlace (LQM):** Monitoreo de calidad
            - **Compresión:** Stacker y Predictor
            - **Multienlace:** Combinación de múltiples enlaces
            - **Compatibilidad:** Funciona con dispositivos no-Cisco

            ### Arquitectura en Capas
            PPP opera sobre:
            - **Medios Sincrónicos:** Líneas arrendadas
            - **Medios Asincrónicos:** Dial-up con módem

            Comparación con OSI:
            - Misma capa física
            - LCP y NCP distribuyen funciones de capas 2 y 3
        """.trimIndent()),

        SubmoduleEntity(id = 20, moduleId = 4, title = "LCP y NCP", contentMd = """
            ### LCP (Link Control Protocol)
            Funciona en la capa de enlace de datos y cumple funciones críticas:

            **Funciones de LCP:**
            - Establecimiento del enlace punto a punto
            - Negociación de opciones de control
            - Configuración automática de encapsulación
            - Autenticación, compresión, detección de errores

            **Opciones de LCP:**
            - Autenticación (PAP/CHAP)
            - Compresión
            - Detección de errores (números mágicos)
            - Devolución de llamada (callback)

            ### NCP (Network Control Protocols)
            Negocia los protocolos de capa 3 para transporte de datos:

            **Protocolos NCP:**
            - **IPCP (0x8021):** Para IPv4
            - **IPv6CP (0x8057):** Para IPv6

            **Funciones de NCP:**
            - Configuración de protocolos de capa de red
            - Compresión de encabezados TCP/IP
            - Asignación de direcciones IP

            ### Estructura de Trama PPP
            ```
            [Flag][Address][Control][Protocol][Data][FCS][Flag]
            ```
            - **Flag:** 01111110 (inicio/fin de trama)
            - **Address:** 11111111 (difusión)
            - **Control:** 00000011 (transmisión no secuencial)
            - **Protocol:** 2 bytes (protocolo encapsulado)
            - **Data:** Datagrama del protocolo
            - **FCS:** Verificación de errores (16 bits)
        """.trimIndent()),

        SubmoduleEntity(id = 21, moduleId = 4, title = "Sesiones PPP", contentMd = """
            ### Establecimiento de Sesión PPP
            PPP establece una sesión en tres fases:

            **Fase 1: Establecimiento del Enlace (LCP)**
            - Negociación de opciones de configuración
            - Completada cuando se recibe Configure-Ack
            - Incluye autenticación si está configurada

            **Fase 2: Calidad del Enlace (Opcional)**
            - LCP prueba la calidad del enlace
            - Determina si es suficiente para protocolos de capa de red

            **Fase 3: Configuración de Protocolos de Red (NCP)**
            - NCP configura protocolos de capa de red
            - IPCP para IPv4, IPv6CP para IPv6
            - El enlace pasa a estado abierto

            ### Operación de LCP
            **Tipos de Tramas LCP:**
            - **Establecimiento de enlaces:** Configure-Request, Configure-Ack
            - **Mantenimiento de enlaces:** Echo-Request, Echo-Reply
            - **Terminación de enlaces:** Terminate-Request, Terminate-Ack

            **Proceso de Negociación:**
            1. Dispositio A envía Configure-Request
            2. Dispositio B responde con Configure-Ack (acepta) o Configure-Nak/Reject (rechaza)
            3. Si acepta, procede a autenticación y configuración NCP

            ### Terminación de Sesión
            PPP puede terminar el enlace por:
            - Pérdida de portadora
            - Error de autenticación
            - Fallo de calidad del enlace
            - Temporizador de inactividad
            - Cierre administrativo
        """.trimIndent()),

        SubmoduleEntity(id = 22, moduleId = 4, title = "Configuración y Autenticación PPP", contentMd = """
            ### Configuración Básica PPP
            ```bash
            Router(config)# interface Serial0/0/0
            Router(config-if)# encapsulation ppp
            Router(config-if)# ip address 10.0.1.1 255.255.255.252
            ```

            ### Comandos de Configuración Avanzada
            **Compresión:**
            ```bash
            Router(config-if)# compress [predictor | stac]
            ```

            **Calidad de Enlace:**
            ```bash
            Router(config-if)# ppp quality percentage
            ```

            **Multienlace PPP:**
            ```bash
            Router(config-if)# ppp multilink
            Router(config-if)# multilink-group group-number
            ```

            ### Autenticación PPP
            **PAP (Password Authentication Protocol):**
            - Bidireccional, sin cifrado
            - Usuario/contraseña en texto claro
            - Menos seguro

            **CHAP (Challenge Handshake Authentication Protocol):**
            - Protocolo de enlace de tres vías
            - Usa hash unidireccional
            - Desafíos periódicos
            - Más seguro

            **Configuración de Autenticación:**
            ```bash
            Router(config-if)# ppp authentication [chap | pap | chap pap | pap chap]
            ```

            **Configuración CHAP:**
            ```bash
            Router(config)# username remote-router password same-password
            Router(config)# interface Serial0/0/0
            Router(config-if)# ppp authentication chap
            ```

            ### Verificación
            Comandos útiles:
            - `show interfaces serial` - Estado PPP
            - `show ppp multilink` - Información multienlace
            - `debug ppp negotiation` - Depuración de negociación
            - `debug ppp authentication` - Depuración de autenticación
        """.trimIndent()),

        // === MÓDULO 5: CONEXIONES DE ACCESO REMOTO ===

        SubmoduleEntity(id = 23, moduleId = 5, title = "Tecnologías de Banda Ancha", contentMd = """
            ### Comparación de Tecnologías de Acceso

            **Cable:**
            - **Ventajas:** Alta velocidad, amplia disponibilidad
            - **Desventajas:** Ancho de banda compartido, velocidades variables en horas pico

            **DSL:**
            - **Ventajas:** No es medio compartido, conexión directa al DSLAM
            - **Desventajas:** Limitado por distancia (max 5.46 km), ancho de banda limitado

            **Fibra hasta el Hogar:**
            - **Ventajas:** Máxima velocidad y confiabilidad
            - **Desventajas:** Disponibilidad limitada, costo de instalación

            **Datos Móviles:**
            - **Ventajas:** Movilidad, cobertura amplia
            - **Desventajas:** Cobertura variable, límites de datos

            **Wi-Fi Municipal:**
            - **Ventajas:** Bajo costo o gratuito
            - **Desventajas:** Disponibilidad limitada en municipios

            **Satélite:**
            - **Ventajas:** Disponible en áreas remotas
            - **Desventajas:** Alto costo, latencia alta, capacidad limitada

            ### Factores de Selección
            Al elegir tecnología de banda ancha considerar:
            - Requisitos de ancho de banda
            - Ubicación geográfica
            - Costo vs. presupuesto
            - Disponibilidad de proveedores
            - Requisitos de confiabilidad
        """.trimIndent()),

        SubmoduleEntity(id = 24, moduleId = 5, title = "PPPoE", contentMd = """
            ### ¿Qué es PPPoE?
            PPP over Ethernet (PPPoE) es un protocolo de red para encapsular tramas PPP dentro de Ethernet. Combina la autenticación PPP con la conectividad Ethernet.

            ### Componentes PPPoE
            **Cliente PPPoE:**
            - Software en el router del cliente
            - Inicia conexión PPPoE
            - Maneja autenticación y configuración

            **Servidor PPPoE:**
            - Concentrador de acceso (BRAS) del ISP
            - Termina sesiones PPPoE
            - Proporciona acceso a Internet

            ### Fases de PPPoE
            **Fase 1: Descubrimiento**
            - Cliente busca servidores disponibles
            - Mensajes: PADI, PADO, PADR, PADS
            - Establece ID de sesión PPPoE

            **Fase 2: Sesión PPP**
            - Negociación LCP
            - Autenticación (PAP/CHAP)
            - Configuración NCP
            - Transferencia de datos

            ### Configuración PPPoE en Cisco
            ```bash
            # Configuración del cliente
            Router(config)# interface dialer1
            Router(config-if)# ip address negotiated
            Router(config-if)# encapsulation ppp
            Router(config-if)# ppp authentication chap
            Router(config-if)# ppp chap hostname username
            Router(config-if)# ppp chap password password
            Router(config-if)# dialer pool 1

            # Asociación con interfaz física
            Router(config)# interface g0/1
            Router(config-if)# pppoe enable
            Router(config-if)# pppoe-client dial-pool-number 1
            ```

            ### Ventajas de PPPoE
            - **Autenticación:** Control de acceso por usuario
            - **Contabilidad:** Medición de uso
            - **Asignación Dinámica:** Direcciones IP dinámicas
            - **Compatibilidad:** Funciona con infraestructura existente
        """.trimIndent()),

        SubmoduleEntity(id = 25, moduleId = 5, title = "VPN (Redes Privadas Virtuales)", contentMd = """
            ### Introducción a VPN
            Una VPN extiende una red privada a través de una red pública, permitiendo a usuarios enviar y recibir datos como si sus dispositivos estuvieran conectados directamente a la red privada.

            ### Beneficios de VPN
            - **Costo:** Elimina líneas arrendadas costosas
            - **Seguridad:** Cifrado de datos en tránsito
            - **Escalabilidad:** Fácil agregar nuevos sitios/usuarios
            - **Movilidad:** Acceso remoto seguro desde cualquier lugar

            ### Tipos de VPN
            **VPN de Acceso Remoto:**
            - Para teletrabajadores y usuarios móviles
            - Cliente a gateway VPN
            - Ejemplo: SSL VPN para navegadores

            **VPN Sitio a Sitio:**
            - Conecta oficinas remotas
            - Gateway a gateway VPN
            - Dos tipos: Basada en IPsec y basada en SSL

            **VPN de Capa 2:**
            - Extiende LANs a través de WAN
            - Ejemplos: PPTP, L2TP, MPLS

            ### Tecnologías VPN
            **IPsec VPN:**
            - Estándar abierto para VPN seguras
            - Cifrado fuerte (AES, 3DES)
            - Modos: Transporte y Túnel

            **SSL VPN:**
            - Acceso a través de navegador web
            - Sin software cliente específico
            - Cifrado TLS/SSL

            **DMVPN:**
            - VPN dinámica multipunto
            - Escalable para múltiples sitios
            - Configuración simplificada

            ### Consideraciones de Seguridad
            - **Cifrado:** Protección de datos confidenciales
            - **Autenticación:** Verificación de identidad
            - **Integridad:** Detección de alteración de datos
            - **Anti-replay:** Prevención de ataques de repetición
        """.trimIndent()),

        SubmoduleEntity(id = 26, moduleId = 5, title = "IPsec y GRE", contentMd = """
            ### IPsec (Internet Protocol Security)
            IPsec proporciona seguridad a nivel de red mediante autenticación y cifrado de paquetes IP.

            **Componentes IPsec:**
            - **AH (Authentication Header):** Autenticación e integridad
            - **ESP (Encapsulating Security Payload):** Cifrado y autenticación
            - **IKE (Internet Key Exchange):** Intercambio seguro de claves

            **Modos de Operación:**
            - **Modo Transporte:** Solo carga útil cifrada
            - **Modo Túnel:** Paquete completo cifrado (para VPNs)

            ### GRE (Generic Routing Encapsulation)
            GRE encapsula un protocolo dentro de otro protocolo, creando túneles virtuales punto a punto.

            **Características GRE:**
            - Transporta protocolos no-IP (IPX, AppleTalk)
            - Soporta multidifusión y routing dinámico
            - Menor overhead que IPsec en modo túnel

            **GRE over IPsec:**
            Combina ventajas de ambos:
            - GRE proporciona flexibilidad de protocolo
            - IPsec proporciona seguridad
            - Configuración común para VPNs sitio a sitio

            ### Configuración GRE
            ```bash
            # Configuración del túnel GRE
            Router(config)# interface tunnel0
            Router(config-if)# ip address 192.168.100.1 255.255.255.252
            Router(config-if)# tunnel source serial0/0/0
            Router(config-if)# tunnel destination 203.0.113.2
            Router(config-if)# tunnel mode gre ip
            ```

            ### Configuración IPsec
            ```bash
            # Configuración de transform set
            Router(config)# crypto ipsec transform-set MYSET esp-aes 256 esp-sha-hmac
            Router(config)# crypto map MYMAP 10 ipsec-isakmp
            Router(config-crypto-map)# set peer 203.0.113.2
            Router(config-crypto-map)# set transform-set MYSET
            Router(config-crypto-map)# match address 100

            # Aplicación a interfaz
            Router(config)# interface serial0/0/0
            Router(config-if)# crypto map MYMAP
            ```

            ### Verificación
            Comandos útiles:
            - `show crypto session` - Sesiones IPsec activas
            - `show crypto ipsec sa` - Asociaciones de seguridad
            - `show interface tunnel` - Estado de túneles GRE
            - `debug crypto` - Depuración de IPsec
        """.trimIndent()),

        SubmoduleEntity(id = 27, moduleId = 5, title = "Solución de Problemas en Acceso Remoto", contentMd = """
            ### Metodología de Solución de Problemas
            **Enfoque Sistemático:**
            1. **Definir el Problema:** Síntomas específicos
            2. **Recopilar Información:** Datos relevantes
            3. **Analizar Información:** Identificar causas posibles
            4. **Eliminar Posibilidades:** Probar hipótesis
            5. **Proponer Solución:** Implementar corrección

            ### Problemas Comunes PPPoE
            **No se establece conexión:**
            - Verificar credenciales PPPoE
            - Confirmar configuración de interfaz dialer
            - Verificar estado de interfaz física

            **Conexión se cae frecuentemente:**
            - Verificar timeout de sesión PPPoE
            - Monitorear calidad de señal
            - Revisar configuración MTU

            **Comandos de Verificación PPPoE:**
            ```bash
            show pppoe session    # Sesiones PPPoE activas
            show pppoe statistics # Estadísticas PPPoE
            debug pppoe events    # Eventos PPPoE
            debug ppp negotiation # Negociación PPP
            ```

            ### Problemas Comunes VPN
            **IPsec no establece túnel:**
            - Verificar políticas de seguridad coincidentes
            - Confirmar configuración de peers
            - Verificar rutas para tráfico VPN

            **Problemas de rendimiento VPN:**
            - Verificar sobrecarga de CPU por cifrado
            - Monitorear ancho de banda disponible
            - Revisar configuración de fragmentación MTU

            **Comandos de Verificación VPN:**
            ```bash
            show crypto isakmp sa    # Asociaciones IKE
            show crypto ipsec sa     # Asociaciones de seguridad
            show crypto engine connections active # Conexiones cifradas
            ```

            ### Herramientas de Diagnóstico
            **Comandos Básicos:**
            - `ping` - Prueba de conectividad básica
            - `traceroute` - Ruta de paquetes
            - `show interfaces` - Estado de interfaces
            - `show running-config` - Configuración actual

            **Comandos Específicos:**
            - `debug ppp authentication` - Autenticación PPP
            - `debug crypto isakmp` - Negociación IKE
            - `debug ip packet` - Paquetes IP (usar con cuidado)

            ### Mejores Prácticas
            - Documentar configuraciones
            - Monitorear rendimiento continuo
            - Mantener firmware actualizado
            - Implementar backup de configuración
            - Establecer línea base de rendimiento
        """.trimIndent())
    )
}