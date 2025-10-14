CampusConnect

CampusConnect is a social networking mobile application designed for university and college students. The app allows users to create posts, share images, chat in real-time, and stay connected with their peers using Firebase as the backend.


## Features

* Firebase Authentication for secure user login and registration
* Home Feed that displays all user posts from the Firebase Realtime Database
* Create Post page with image upload and caption functionality
* Chat section for real-time communication between users
* Firebase Storage integration for managing uploaded images
* Realtime Database for live updates on posts and messages
* Bottom Navigation for switching between pages (Home, Chat, Create Post, and Profile)

---

## Tech Stack

* **Language:** Kotlin
* **Framework:** Android Jetpack
* **Database:** Firebase Realtime Database
* **Authentication:** Firebase Authentication
* **Storage:** Firebase Storage
* **UI Design:** XML Layouts

---

## Setup Instructions

1. Clone the repository:

   ```bash
   git clone https://github.com/ST10359984/CampusConnect.git
   ```

2. Open the project in **Android Studio**

3. Connect the app to your **Firebase Project**

4. Enable the following Firebase services:

   * Authentication
   * Realtime Database
   * Storage

5. Set temporary Firebase rules for testing:

   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```

6. Run the app on an Android device or emulator.

---

## Project Structure

```
com.example.campusconnect/
│
├── MainActivity.kt
│
├── fragments/
│   ├── HomeFragment.kt
│   ├── ChatFragment.kt
│   ├── CreatePostFragment.kt
│   └── ProfileFragment.kt
│
├── adapters/
│   ├── PostAdapter.kt
│   └── ChatAdapter.kt
│
├── models/
│   ├── Post.kt
│   └── ChatMessage.kt
│
├── layout/
│   ├── activity_main.xml
│   ├── fragment_home.xml
│   ├── fragment_chat.xml
│   ├── fragment_create_post.xml
│   ├── fragment_profile.xml
│   └── item_post.xml
│
├── menu/
│   └── bottom_nav_menu.xml
│
├── drawable/
│   ├── edittext_bg.xml
│   └── app_backgrounds, icons, and other UI assets
│
└── manifests/
    └── AndroidManifest.xml
```

---

## Contributors

* **Amogelang**
* **Avuyile**
* **Kabelo**

Do you want me to tweak it a bit to sound more like a formal **university project summary** (for a PoE or report)?
