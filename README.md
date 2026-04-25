# Xatruch POS

Xatruch POS is a modern, cloud-synced Point of Sale system designed for restaurants and small businesses. It allows users to manage their menu, track inventory in real-time, and process sales efficiently on Android devices.

## 🚀 Key Features

- **Sales Management (Caja):**
  - Real-time cart management.
  - Sequential invoice numbering.
  - Support for "Consumidor Final" and registered clients (RTN support).
  - Built-in search functionality to quickly find products in large menus.
- **Inventory & Recipes:**
  - Separate management of Menu items and Inventory supplies.
  - **Recipe System:** Link menu products to inventory ingredients for automatic stock deduction during sales.
  - Fallback name-matching for simple items (e.g., drinks).
- **Cloud Synchronization:**
  - Powered by **Firebase (Firestore & Auth)**.
  - Real-time synchronization across multiple devices.
  - Offline support via **Room Database**.
- **Printing:**
  - Professional invoice generation.
  - Support for thermal printers and PDF export.
  - High-fidelity capture including business logos.
- **Modern UI:**
  - Fully compatible with **Dark Mode**.
  - Optimized for both vertical (phone) and horizontal (tablet) orientations.

## 🛠 Tech Stack

- **Language:** Kotlin
- **Local Database:** Room (Architecture Components)
- **Cloud Database:** Firebase Firestore
- **Authentication:** Firebase Auth
- **Image Loading:** Coil
- **UI Components:** Material Design 2
- **Dependency Management:** Gradle Version Catalog (.toml)

## 📁 Project Structure

- `data/`: Room entities, DAOs, and database configuration.
- `repository/`: Data management logic and Firebase synchronization (SyncManager).
- `ui/`: UI components organized by feature (Auth, Caja, Menu, Settings).
- `util/`: Helper classes for images, printing, and dialogs.