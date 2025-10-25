# 🎓 Aprende +: Estudio Modular con Tests Generados por IA (JOSPROX MX)

> **Eslogan:** La forma más inteligente de aprender cualquier tema. Estructura tu conocimiento, genera exámenes únicos y gestiona tu contenido de estudio.

Una aplicación de estudio nativa de Android diseñada para optimizar el aprendizaje de cualquier tema a través de una jerarquía de contenido clara: **Materias → Módulos → Submódulos**. Construida al 100% con **Kotlin** y la modernidad de **Jetpack Compose (Material 3)**.

---

## ✨ Características Estelares

### 🧠 Aprendizaje Dinámico y Modular

El diseño se basa en un flujo de estudio intuitivo:

1.  **Materias (Ej. "Redes de Computadoras")**
2.  **Módulos (Ej. "Modelo OSI")**
3.  **Submódulos (Contenido en Markdown)**

Esto permite una organización del conocimiento escalable, ideal para temas técnicos o académicos. 

### 🤖 Generación de Exámenes con IA

* **Tests Únicos:** Cada examen para un módulo es generado dinámicamente utilizando la **API de GroqCloud**, asegurando que el estudiante nunca repita las mismas preguntas.
* **Gestión de Exámenes:** Permite **continuar** un test incompleto y ofrece una revisión detallada de la **calificación** para ver la pregunta, tu respuesta y la solución correcta.
* **Reintentar:** Opción para borrar el historial de un módulo y generar un set de preguntas completamente nuevo.

### 📚 Gestión de Contenido Personalizado (UGC)

* **Importación Sencilla:** Los usuarios pueden añadir sus propias materias de estudio a la aplicación importando un simple archivo **JSON** con el formato predefinido (ver sección `Formato de Importación`).
* **Control Total:** Administra tu biblioteca con facilidad, incluyendo la eliminación rápida de materias no deseadas.

---

## 🛠️ Stack Tecnológico (Modernidad y Rendimiento)

El proyecto está construido bajo una arquitectura moderna para garantizar alto rendimiento y mantenibilidad:

| Categoría | Tecnología | Propósito |
| :--- | :--- | :--- |
| **Frontend/UI** | **Jetpack Compose (Material 3)** | Interfaz de usuario moderna y declarativa. |
| **Lenguaje** | **100% Kotlin** | Lenguaje primario. |
| **Arquitectura** | **MVVM** | Separación limpia de la lógica de negocio y la UI. |
| **Persistencia** | **Room** | Base de datos local para almacenar el contenido de estudio y el progreso. |
| **Asincronía** | **Coroutines y Flows** | Manejo de tareas en segundo plano y flujos de datos reactivos. |
| **Inyección** | **Hilt (Dagger-Hilt)** | Gestión robusta de dependencias. |
| **IA/Network** | **GroqCloud API** | Generación dinámica de preguntas para los exámenes. |
| **Utilidades** | `kotlinx.serialization` | Parseo eficiente del formato JSON. |
| **Renderizado** | `compose-richtext` | Visualización del contenido de estudio escrito en **Markdown**. |

---

## 🚀 Instalación y Configuración del Entorno

Sigue estos pasos para poner la aplicación en funcionamiento:

1.  **Clonar el Repositorio:**
    ```bash
    git clone https://github.com/josprox/Aprende-mas
    ```
2.  **Abrir Proyecto:** Abre la carpeta clonada en **Android Studio (versión 2023.2 o superior)**.

3.  **Configurar la API Key (¡CRÍTICO!):**
    * Obtén tu clave de API gratuita en el portal de [GroqCloud](https://console.groq.com/keys).
    * Navega al archivo `data/network/GroqApiService.kt`.
    * **Reemplaza** la variable `API_KEY` con tu clave personal:
        ```kotlin
        // Reemplaza "TU_API_KEY_AQUI"
        private const val API_KEY = "TU_API_KEY_AQUI" 
        ```

4.  **Ejecutar:** Sincroniza Gradle, construye y ejecuta el proyecto en un emulador o dispositivo físico.

---

## 📄 Formato de Importación JSON (UGC Schema)

Para añadir tu propio contenido de estudio (función "Añadir Materia"), utiliza estrictamente el siguiente esquema de archivo `.json`. Este archivo será parseado y persistido localmente.

**Estructura Requerida (`mi_materia.json`):**

```json
{
  "name": "Seguridad Informática",
  "modules": [
    {
      "title": "Criptografía Básica",
      "shortDescription": "Aprende sobre cifrado simétrico y asimétrico.",
      "submodules": [
        {
          "title": "Cifrado Simétrico",
          "contentMd": "### ¿Qué es?\nEl cifrado simétrico usa la misma clave para cifrar y descifrar..."
        },
        {
          "title": "Cifrado Asimétrico",
          "contentMd": "### ¿Qué es?\nEl cifrado asimétrico usa una clave pública y una privada..."
        }
      ]
    }
  ]
}
````

-----

## 📜 Licencia y Contacto

Este proyecto está bajo una **Licencia de Código Fuente Consultable y Contribución Restringida (LCSCR)**.

* El código fuente está disponible para **consulta y aportación** (contribuciones).
* **Prohibida** la modificación, redistribución, republicación o uso comercial del código principal.

**Desarrollado por:** Melchor Estrada José Luis - JOSPROX MX

**Soporte Oficial y Consultas de Licencia:**
[https://josprox.com/soporte/](https://josprox.com/soporte/)
