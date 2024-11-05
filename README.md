# MedApp

MedApp is an Android application that allows users to take pictures or select images from the gallery, classify them using a machine learning model, and display the results.

## Prerequisites

- Android Studio
- Java Development Kit (JDK) 17 or higher
- Android SDK
- Gradle

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/Skriller18/MedApp_Deployment.git
cd MedApp_Deployment
```

## Setup

1. Open the project in Android Studio.
2. Sync the project with Gradle files by clicking on File > Sync Project with Gradle Files.

## Build the Project
To build the project, run the following command in the terminal:
```bash
./gradlew build
```

## Run the App
To run the app on an emulator or a connected device, use the following command:
```bash
./gradlew installDebug
```
Alternatively, you can run the app directly from Android Studio by clicking on the Run button.

## Permissions
The app requires the following permissions:
1. Camera
2. Internet
These permissions are declared in the AndroidManifest.xml file.

## Usage
1. Launch the app on your device.
2. Use the "Take Picture" button to capture an image using the camera.
3. Use the "Launch Gallery" button to select an image from the gallery.
4. The app will classify the image and display the result.

## Project Structure
```
.
├── .gitignore
├── .gradle/
├── .idea/
├── app/
│   ├── build/
│   ├── src/
│   ├── [build.gradle.kts](http://_vscodecontentref_/0)
│   ├── proguard-rules.pro
│   └── release/
├── [build.gradle.kts](http://_vscodecontentref_/1)
├── [description.json](http://_vscodecontentref_/2)
├── Descriptions/
├── gradle/
├── [gradle.properties](http://_vscodecontentref_/3)
├── gradlew
├── [gradlew.bat](http://_vscodecontentref_/4)
├── [key.jks](http://_vscodecontentref_/5)
├── [local.properties](http://_vscodecontentref_/6)
└── [settings.gradle.kts](http://_vscodecontentref_/7)
```
