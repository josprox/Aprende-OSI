# App de Estudio Modular (RedesOSI)

Una aplicación de estudio nativa de Android, construida 100% con Kotlin y Jetpack Compose. Está diseñada para permitir a los usuarios aprender cualquier tema a través de una estructura modular de **Materias -> Módulos -> Submódulos**.

La característica principal de la app es su capacidad de **generar exámenes únicos usando IA (Groq)** y permitir a los usuarios **importar y gestionar su propio contenido** de estudio a través de archivos JSON.

## ✨ Características Principales

* **Sistema de Estudio Modular:** Aprende a través de una jerarquía clara:
    * **Materias**: El tema general (ej. "Redes de Computadoras", "Historia Universal").
    * **Módulos**: Secciones principales del tema (ej. "Modelo OSI", "Revolución Francesa").
    * **Submódulos**: El contenido de estudio detallado, escrito en Markdown.

* **Exámenes Generados por IA:** Los exámenes para cada módulo son creados dinámicamente usando la **API de GroqCloud**. Esto asegura que cada test sea diferente.

* **Gestión Avanzada de Exámenes:**
    * **Resumir Exámenes:** Si dejas un examen a medias, puedes volver al menú "Test" y continuar exactamente donde te quedaste.
    * **Revisar Exámenes:** Una vez completado, puedes ir a la sección "Calificación" o "Test" para revisar cada pregunta, ver tu respuesta y cuál era la correcta.

* **Gestión de Contenido (UGC):**
    * **Importar Materias:** Añade tu propio contenido de estudio a la app usando un archivo `.json` con un formato predefinido.
    * **Eliminar Materias:** Borra materias (y todo su contenido asociado) con una simple pulsación larga en la pantalla principal.

* **Regeneración de Exámenes:** ¿Quieres volver a probar un módulo con preguntas nuevas? Puedes optar por borrar tu historial y generar un set de preguntas completamente nuevo desde la pantalla de detalle del módulo.

## 🛠️ Stack Tecnológico

* **UI:** Jetpack Compose (Material 3).
* **Lenguaje:** 100% Kotlin.
* **Asincronía:** Coroutines y Flows.
* **Arquitectura:** MVVM (Model-View-ViewModel).
* **Inyección de Dependencias:** Hilt (Dagger-Hilt).
* **Base de Datos:** Room (para persistencia local).
* **Navegación:** Navigation Compose.
* **Parseo de JSON:** `kotlinx.serialization`.
* **Renderizado de Markdown:** [compose-richtext (Markdown)](https://github.com/halilozercan/compose-richtext).
* **IA (Generación de Tests):** GroqCloud API.

## 🚀 Instalación y Configuración

1.  Clona el repositorio:
    ```bash
    git clone https://github.com/josprox/Aprende-OSI
    ```
2.  Abre el proyecto en Android Studio.
3.  **Configurar la API Key (¡Importante!)**
    * Necesitarás una clave de API gratuita de [GroqCloud](https://console.groq.com/keys).
    * Ve al archivo `data/network/GroqApiService.kt`.
    * Busca la variable `API_KEY` y reemplaza `"TU_API_KEY_AQUI"` con tu clave real.
4.  Construye y ejecuta la aplicación.

## 📄 Formato de Importación JSON

Para usar la función "Añadir Materia", necesitas crear un archivo `.json` con la siguiente estructura. La app parseará este archivo y lo guardará en la base de datos local.

**Ejemplo (`mi_materia.json`):**

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
    },
    {
      "title": "Malware",
      "shortDescription": "Tipos de software malicioso.",
      "submodules": [
        {
          "title": "Virus y Gusanos",
          "contentMd": "Un virus se adjunta a un programa, un gusano se replica por la red..."
        }
      ]
    }
  ]
}
```


## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.
