# TankCheck - Container Status Tracker

TankCheck is an Android application designed to monitor and manage the status of a container (full/empty) in real-time using `Firebase` as the backend. The app features a persistent status bar notification and daily automatic reset functionality.

<p align="center">
  <img src="./public/screen.jpeg" width="300" alt="TankCheck Screenshot">
</p>

<p align="center">
  <img src="./public/toggle.jpeg" width="200" alt="TankCheck Screenshot">
</p>

## Key Features

- **Real-time Status Tracking**: *Monitor container status (full/empty) with instant updates*
- **Persistent Notification**: *Always-visible status bar indicator (red for full, green for empty)*
- **Daily Auto-Reset**: *Automatic reset to "full" status every day at 3:00 AM*
- **Firebase Integration**: *Cloud-synced status across all devices*
- **Simple Interface**: *One-tap status toggle between full/empty states*
- **Anonymous Authentication**: *Secure Firebase access without user registration*

## Technical Implementation

### Core Components

1. **MainActivity**: 
   - Handles UI and user interactions
   - Manages Firebase authentication (anonymous sign-in)
   - Displays current container status
   - Provides toggle button for status changes

2. **StatusBarService**:
   - Foreground service with persistent notification
   - Listens for Firebase status changes
   - Updates notification color and text based on status
   - Manages daily reset alarm

3. **DailyResetReceiver**:
   - Broadcast receiver for daily reset at 3:00 AM
   - Updates Firebase status to "full"
   - Restarts the alarm for next day

### Firebase Structure

- Single path: `containerStatus` (boolean)
  - `true` = Container is full
  - `false` = Container is empty

## Requirements

- Android SDK 21+ (Android 5.0 Lollipop)
- Google Play services
- Internet connection
- `SCHEDULE_EXACT_ALARM` permission (for daily reset)

## Installation

1. Clone this repository
2. Open in Android Studio
3. Add your `google-services.json` file from Firebase
4. Build and run

## Usage

1. Launch the app
2. The system will automatically authenticate anonymously
3. Current status is displayed with icon and text
4. Tap button to toggle between full/empty states
5. Status is immediately updated in Firebase and notification

## Notification Behavior

- 🔴 Red notification: Container is full
- 🟢 Green notification: Container is empty
- Persistent notification cannot be dismissed
- Tapping notification opens the app

## Daily Reset Logic

The container status is automatically reset to "full" every day at 3:00 AM local time. This is handled by:

1. `AlarmManager` sets exact alarm for 3:00 AM
2. `DailyResetReceiver` triggers at scheduled time
3. Firebase status is updated to `true`
4. Notification updates to reflect new status
5. New alarm is set for next day

## Contributing

Contributions are welcome! Please open an issue or pull request for any improvements.

## License

[GPL-3.0 license](https://github.com/comitanigiacomo/TankCheck?tab=GPL-3.0-1-ov-file)