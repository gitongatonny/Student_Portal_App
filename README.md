# CS-Student-Portal-App

## Overview

CS-Student-Portal-App is a robust Android application designed to streamline academic processes for computer science students and lecturers. Built with Java and integrated with Firebase, this app offers a seamless experience for course management and grade tracking.

![App Home Screen](path/to/home_screen_screenshot.png)

## Tech Stack

### Frontend
- Java
- Android SDK
- AndroidX libraries
- Material Design components

### Backend
- Firebase Authentication
- Firebase Firestore

## Features and Functionality

### 1. User Authentication
- Separate login portals for students and lecturers
- Secure registration process with email verification

![Login Screen](path/to/login_screen_screenshot.png)

### 2. Student Portal
- Units registration for the current semester
- View enrolled units
- Access grades for individual units

### 3. Lecturer Portal
- Course management
- View list of registered students for each course they teach
- Input student marks and get the grades automatically calculated

![Grade Management Screen](path/to/grade_management_screenshot.png)

## App Flow

1. **Launch**: Users are greeted with a main screen offering options to log in as a student or lecturer.

2. **Authentication**: 
   - New users can register with their details.
   - Existing users can log in securely.

3. **Dashboard**:
   - Students access their course list and grades.
   - Lecturers view their assigned courses and student lists.

4. **Course Management**:
   - Students can register for courses and view their enrolled units.
   - Lecturers can see the list of students registered for their courses.

5. **Grade Input and Viewing**:
   - Lecturers can input CAT and exam marks for each student in their courses.
   - The system automatically calculates the final grade based on the input marks.
   - Students can view their updated grades for each enrolled unit.

6. **Profile Management**:
   - Users can view and edit their profile information.
   - Password change functionality is available for enhanced security.

## Getting Started

To run this project locally:

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Configure Firebase:
   - Create a Firebase project
   - Add your `google-services.json` file to the app directory
5. Build and run the app on an emulator or physical device

## Screenshots

[Insert relevant screenshots here]

## Problem Faced
Report Generation could not be implemented due to constraints in terms of Firebase. It seems Firebase cannot be used for complex and dense databases. 
Also, the students can't register for units outside the current semester due to this.

## Contributing

You are welcome to the CS-Student-Portal-App! 

## License

This project is licensed under the [MIT] LICENSE- see the LICENSE.md file for details.
