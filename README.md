# Xatruch POS

Xatruch POS es un sistema de punto de venta moderno, sincronizado en la nube, diseñado para restaurantes y pequeños negocios. Permite a los usuarios gestionar su menú, controlar el inventario en tiempo real y procesar las ventas de forma eficiente en dispositivos Android.

## 🚀 Características principales

- **Gestión de ventas (Caja):**

- Gestión de carritos en tiempo real.

- Numeración secuencial de facturas.

- Compatibilidad con "Consumidor Final" y clientes registrados (compatibilidad con RTN).

- Función de búsqueda integrada para encontrar rápidamente productos en menús extensos.

- **Inventario y recetas:**

- Gestión independiente de los artículos del menú y del inventario.

- **Sistema de recetas:** Vincula los productos del menú con los ingredientes del inventario para la deducción automática de existencias durante las ventas.

- Coincidencia de nombres para artículos sencillos (p. ej., bebidas).

- **Sincronización en la nube:**

- Impulsado por **Firebase (Firestore y autenticación)**.

- Sincronización en tiempo real entre múltiples dispositivos.

- Soporte sin conexión mediante **Room Database**.

- **Impresión:**

- Generación de facturas profesionales.

- Compatibilidad con impresoras térmicas y exportación a PDF.

- Captura de alta fidelidad, incluyendo logotipos de empresas.

- **Interfaz de usuario moderna:**

- Totalmente compatible con el **Modo Oscuro**.

- Optimizado para orientación vertical (teléfono) y horizontal (tableta).

## 🛠 Tecnologías utilizadas

- **Lenguaje:** Kotlin
- **Base de datos local:** Room (Componentes de arquitectura)
- **Base de datos en la nube:** Firebase Firestore
- **Autenticación:** Firebase Auth
- **Carga de imágenes:** Coil
- **Componentes de interfaz de usuario:** Material Design 2

- **Gestión de dependencias:** Catálogo de versiones de Gradle (.toml)

## 📁 Estructura del proyecto

- `data/`: Entidades de Room, DAOs y configuración de la base de datos.
- `repository/`: Lógica de gestión de datos y sincronización con Firebase (SyncManager).

- `ui/`: Componentes de la interfaz de usuario organizados por función (Autenticación, Caja, Menú, Configuración).

- `util/`: Clases auxiliares para imágenes, impresión y diálogos.
