# AudioBookPlayer (SoundSaga)

SoundSaga is an Android app that allows users to enjoy audiobooks with seamless playback, chapter navigation, and speed controls. It integrates the **Volley** library for data fetching, **MediaPlayer** for audio playback, and provides features such as **SplashScreen API**, **GridLayoutManager**, **ViewPager2**, and more.

## Features

### MainActivity
- Displays a splash screen followed by a vertically-scrolling list of available audiobooks.
- Portrait orientation shows 2 columns, while landscape orientation displays 4 columns.
- Users can select an audiobook to play or tap the **My Books** icon to view books in progress.

### AudioBookActivity
- Shows the currently playing audiobook with playback starting from the selected book or where the user left off.
- Allows chapter navigation through swiping or using arrow buttons.
- Provides a **seekbar** to track chapter progress and a **playback speed control**.
- The **play/pause** button toggles the playback state.
- Tapping the back button saves the current progress and returns to **MainActivity**.

### MyBooksActivity
- Displays audiobooks in progress, sorted by most-recently-played.

## Download APK for Testing
You can download the APK file for testing from the release below:
- [Download v1.0 Testing APK](https://github.com/kartiknagar333/AudioBookPlayer/releases/tag/v1.0)
<br>  

## Screenshots
- **MainActivity**
![audio](https://github.com/user-attachments/assets/20def6cc-9795-4f2f-ac04-345d4e0366f9)
