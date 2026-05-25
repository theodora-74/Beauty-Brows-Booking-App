# Beauty Excellence 

A native Android booking application designed for home-based beauty salons. Built with Kotlin and Material Design 3.

## Features

- **Appointment Booking** — Clients select a service, location, date, and time slot in a guided flow
- **Booking Codes** — Each booking generates a unique 6-character code for self-service cancellation
- **Admin Dashboard** — Approve or reject appointments with a tabbed overview
- **SMS & WhatsApp Notifications** — Notify clients instantly when their booking status changes
- **Working Hours Management** — Set per-day hours for each location, or define weekly recurring schedules
- **4 Languages** — English, Greek (Ελληνικά), Albanian (Shqip), Russian (Русский)
- **Dark Mode** — Manual light/dark theme toggle
- **Client Access PIN** — Only clients who know the PIN can access the app
- **Two Locations** — Thessaloniki and Litochoro

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| Min SDK | API 26 (Android 8.0) |
| UI | Material Design 3 |
| Storage | SharedPreferences + JSON |
| Architecture | Activity-based with ViewBinding |
| Build | Gradle (Kotlin DSL) |

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1) or newer
- JDK 11+

### Installation

1. Clone the repository:
git clone https://github.com/theodora-74/Beauty-Brows-Booking-App.git
2. Open the `BeautyBooking/` folder in Android Studio
3. Wait for Gradle sync to complete
4. Run on emulator or device (API 26+)

### Default Credentials

| Credential | Value |
|-----------|-------|
| Admin Username | admin |
| Admin Password | 12345 |
| Client Access PIN | 2026 |

The admin can change the client PIN from the dashboard menu.

## Project Structure
BeautyBooking/
├── app/src/main/
│   ├── java/com/beautybooking/app/
│   │   ├── models/          # Appointment, Service
│   │   ├── utils/           # LocalDataManager, Constants, LocaleHelper
│   │   ├── client/          # Booking flow activities + adapters
│   │   ├── admin/           # Dashboard, login, calendar management
│   │   ├── BaseActivity.kt  # Locale + theme application
│   │   └── MainActivity.kt  # Home screen
│   └── res/
│       ├── layout/          # 15 XML layouts
│       ├── drawable/        # Vector icons + backgrounds
│       ├── drawable-night/  # Dark mode drawables
│       ├── values/          # English strings, colours, themes
│       ├── values-el/       # Greek strings
│       ├── values-sq/       # Albanian strings
│       ├── values-ru/       # Russian strings
│       └── values-night/    # Dark mode colours + themes
└── build.gradle.kts

## Licence

This is proprietary software. See [LICENSE](LICENSE) for details.

© 2026 Theodora Filaj. All rights reserved.
Create it as README.md in the BeautyBooking/ folder, then:
git add README.md
git commit -m "Add README"
Or if you haven't pushed yet, just add all three files together:
git add .gitignore LICENSE README.md
git commit -m "Add project documentation"
git push -u origin main --force
