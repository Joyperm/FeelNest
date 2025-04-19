# FeelNest - Project Overview

**FeelNest** is designed for people who want to take control of their mental health and build healthier habits. It helps users recognize emotional patterns they might not notice on their own, allowing them to address their feelings in a positive way. The app also keeps users motivated to maintain a balanced lifestyle by supporting both mental and physical well-being.

## 📽 FeelNest Demo

🎥 [Watch on YouTube](https://youtu.be/xK5C6OzlQwc)

![image](https://github.com/user-attachments/assets/f82f27f1-17d6-4f38-ab22-37905d23db0d)
![image](https://github.com/user-attachments/assets/91bc7d95-7241-4394-8dbc-f0bb83ff7d86)
![image](https://github.com/user-attachments/assets/485ed15b-4ed4-41f2-b678-c5815d703c33)
![image](https://github.com/user-attachments/assets/9094431c-2ec1-4f02-acc5-e30c98c0c1d1)
![image](https://github.com/user-attachments/assets/54336e82-da1d-4471-81cf-70055824a712)
![image](https://github.com/user-attachments/assets/2252fb76-717c-426d-ac10-8687acc7ad6c)
![image](https://github.com/user-attachments/assets/4b6c58bd-1f79-4b79-8d0e-7eca844d53fe)
![image](https://github.com/user-attachments/assets/f9a42f5a-bab2-4a98-9767-e6554d180072)
![image](https://github.com/user-attachments/assets/68322217-a828-4fd0-9699-1b2b69e34b9b)
![image](https://github.com/user-attachments/assets/10dd060a-e09d-42dc-b294-9b56fe2ff2b7)

---

## 🛠 Development & Testing Environment

- **Android Studio Version:** Ladybug 2024.2.1 Patch 1  
- **Programming Language:** Java  
- **Testing Emulator:** Pixel 4a (API 30)  
- **Android SDK Configuration:**  
  - `compileSdk`: 35  
  - `minSdk`: 27  
  - `targetSdk`: 35  

---

## 📦 Dependencies and Technologies Used

### Firebase Integrations

- `com.google.firebase:firebase-auth:22.1.2` - Firebase Authentication  
- `com.google.firebase:firebase-database` - Firebase Realtime Database  
- `com.firebaseui:firebase-ui-database:8.0.2` - Firebase UI bindings  

### UI Libraries

- `de.hdodenhof:circleimageview:3.1.0` - Circle profile images  
- `com.orhanobut:dialogplus:1.11@aar` - Customizable dialogs  
- `com.github.bumptech.glide:glide:4.16.0` - Image loading and caching  
- `com.github.PhilJay:MPAndroidChart:v3.1.0` - Graphs and charting  

### Networking

- `com.android.volley:volley:1.2.1` - Network requests  

---

## 🌐 APIs Used

- **Firebase Authentication** – Manages user login and authentication  
- **Firebase Realtime Database** – Stores and syncs user data  
- **Joke API** – [https://apilist.fun/api/jokeapi](https://apilist.fun/api/jokeapi)  
- **Giphy API** – [https://giphy.com](https://giphy.com) – Fetches random GIFs for jokes  

---

## ✨ Features Developed

### Mood Tracking

- Select mood from predefined categories (e.g., Happy, Sad, Neutral)  
- Add optional notes/comments  
- Track mood trends over time  

### Behavioral Alerts

- Notification when user logs “Sad”  
- Suggests joke section for mood boost  

### Joke Recommendation

- Fetches random jokes from API  
- Helps improve mood with humor  

### Sleep Recommendation

- Personalized sleep tips and suggested durations  

### Dashboard

- Displays mood trends using graphs  
- Shows most frequent moods and mood distribution by month  

### User Profile Settings

- Allows custom profile images from in-app image options  

---



