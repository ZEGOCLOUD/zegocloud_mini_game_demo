# README

This demo is a demonstration of combining audio and video with mini-games in game rooms and live audio rooms, providing a reference for customers on how to increase entertainment for their applications.

## 1. Authentication Information Application
Before accessing mini-games, you need to register for our account on the [ZEGOCLOUD Console](https://console.zegocloud.com/).

And provide the following information for us to configure server authentication information:
Application information:
| Platform | ApplicationID/BundleID | Example | AppName | Example |
| --- |--- | --- | --- | --- |
| Android | xxx | com.zegocloud.minigame.demo | xxx | Mini-Game Demo |
| iOS | xxx | com.zegocloud.minigame.demo | xxx | Mini-Game Demo |

Server authentication URL：

| Interface Name | Description | Test Environment | Formal Environment | Example |
| --- | --- | --- | --- | --- |
| get_sstoken | Get authentication token | xxx | xxx | https://zegocloud.com/get_sstoken |
| update_sstoken | Update authentication token | xxx | xxx | https://zegocloud.com/update_sstoken |
| get_user_info | Get user information | xxx | xxx | https://zegocloud.com/get_user_info |
| report_game_info | Report game data | xxx | xxx | https://zegocloud.com/report_game_info |
| notify | Game notification | xxx | xxx | https://zegocloud.com/notify |
You may refer to [the implementation document of the related interfaces](https://zegocloud.feishu.cn/docx/W1uDd7oBMoVY3AxnBvYcj8IMntc)."

## 2. Run the Demo
### 2.1 Server Deployment
Mini-Game uses server authentication. Therefore, before running the demo, you need to deploy the related server services. You can download the server sample code from [here](https://github.com/ZEGOCLOUD/zegocloud-mini-game-server). After downloading, you need to set the authentication information in `config.js` and then deploy the related services.


> Please note that the interface URL deployed here needs to be consistent with the interface URL provided for obtaining authentication information.

### 2.2 Client Modification
Next, download the Android client code from [here](), and modify the following information:

- Modify the authentication information in the `ZEGOSDKKeyCenter.java` file.
- Modify the `ApplicationID` of the demo, which needs to be consistent with the ApplicationID provided when obtaining authentication information.

After completing the above modifications, you can run the demo and experience the related games.

## 3. Project file directory
```
.
├── README.md                           ---- Project instruction
├── SudMGPSDK                           ---- Mini-Game SDK
├── SudMGPWrapper                       ---- Mini-Game SDK API wrapper
├── app                                 ---- Demo project
│   ├── src/main/java/com/zegocloud/demo/liveaudioroom  ---- Demo project code
│       ├── activity                    ---- Activities used in the demo
│       ├── backend                     ---- Server API requests used in the demo
│       ├── components                  ---- UI components used in the demo
│       ├── internal                    ---- Common functionality encapsulated
│           ├── components              ---- Common components
│           ├── invitation              ---- Invite speaker service for live audio rooms.
│           ├── minigame                ---- Singleton management class for Mini-Game
│           ├── rtc                     ---- Singleton management class for audio and video
│       ├── utils                       ---- Common utility classes
│       ├── ZEGOLiveAudioRoomManager    ---- Singleton management class for business logic
│       ├── ZEGOSDKKeyCenter            ---- Authentication information

```

## 4. How to use source code

### 4.1 Integrate Video Call SDK
Refer to the [Video Call Quick start](https://www.zegocloud.com/docs/video-call/quickstart?platform=android&language=java) document to access Video Call SDK

### 4.2 Integrate Mini-Game SDK
1. Download the latest version of the SDK from [here]().
2. Import `SudMGPSDK` and `SudMGPWrapper` modules through the `File -> New -> Import Module` function in Android Studio.

### 4.3 Copy Project Code
Copy the following folders and files to your project:
- `components`
- `internal`
- `utils`
- `ZEGOLiveAudioRoomManager.java`
- `ZEGOSDKKeyCenter.java`

### 4.4 Code Logic Implementation
#### 4.4.1 SDK Initialization
Initialize the RTC SDK and Mini-Game SDK.

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initZEGOSDK();
    initMiniGameSDK();
}
    
private void initZEGOSDK() {
    ZEGOSDKManager.getInstance().initSDK(getApplication(), ZEGOSDKKeyCenter.appID, ZEGOSDKKeyCenter.appSign);
}

private void initMiniGameSDK() {
    boolean isTestEnv = true;
    MiniGameManager.getInstance().initMiniGameSDK(this, ZEGOSDKKeyCenter.MiniGame_APP_ID, ZEGOSDKKeyCenter.MiniGame_APP_KEY, isTestEnv);
}
```

#### 4.4.2 Set Audio and Video Stream Listeners
Before joining a game room or live audio room, you need to first set up room event listeners.
```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game_room);

    startListenEvent();
}

private  void startListenEvent() {
    ZEGOSDKManager.getInstance().rtcService.setEventHandler(new IZegoEventHandler() {
        @Override
        public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
            super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);

            if (updateType == ZegoUpdateType.ADD) {
                startPlayStream(streamList.get(0).streamID);
            } else {
                stopPlayStream(streamList.get(0).streamID);
            }
        }
    });
}
```

#### 4.4.3 Add Game View
Before loading the game, you need to add the game view.
```java
    private void addGameView() {
        FrameLayout gameContainer = findViewById(R.id.game_container);
        MiniGameManager.getInstance().gameViewLiveData.observe(this, new Observer<View>() {
            public void onChanged(@Nullable View view) {
                if (view == null) {
                    gameContainer.removeAllViews();
                } else {
                    gameContainer.addView(
                            view,
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    );
                    MiniGameManager.getInstance().joinGame();
                    MiniGameManager.getInstance().updateReadyStatus();
                }
            }
        });
    }
```

### 4.4.4 Join the Room and Load the Game
Finally, join the room and load the game to achieve the integration of audio/video and mini-games.

```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    ZEGOSDKManager.getInstance().joinRoom(roomID, new IZegoRoomLoginCallback() {
        @Override
        public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
            if (errorCode != 0) {
                finish();
            } else {
                startPreview();
                startPublish(roomID);

                long gameID = getIntent().getLongExtra("gameID", 0L);
                MiniGameManager.getInstance()
                .loadGame(this, user.userID, roomID, gameID,
                    ZEGOLiveAudioRoomManager.getInstance().getToken());
            }
        }
    });
}
```

### 4.5 Other Logic
#### 4.5.1 Listen to Game Status
The following code listens to changes in the game status and updates the video display accordingly:
```java
MiniGameManager.getInstance().setCallback(new MiniGameManager.MiniGameCallback() {
    @Override
    public void onGameMGCommonGameState(SudMGPMGState.MGCommonGameState model) {
        TextureView remoteUserView = findViewById(R.id.remoteUserView);
        TextureView localUserView = findViewById(R.id.preview);
        // gameState, 0: Game not started, 1: Game loading, 2: Game in progress.
        if (model.gameState == 2) {
remoteUserView.setVisibility(View.VISIBLE);
            localUserView.setVisibility(View.VISIBLE);
        } else {
remoteUserView.setVisibility(View.INVISIBLE);
            localUserView.setVisibility(View.INVISIBLE);
        }
    }
});
```

#### 4.5.2 Listen to Game Avatar Coordinates
The following code implements the integration of video display and mini-games by listening to game avatar coordinates:
```java
MiniGameManager.getInstance().setCallback(new MiniGameManager.MiniGameCallback() {
    @Override
    public void onGamePlayerIconPositionUpdate(SudMGPMGState.MGCommonGamePlayerIconPosition model) {
        TextureView view = findViewById(R.id.remoteUserView);
        if (Objects.equals(model.uid, ZEGOLiveAudioRoomManager.getInstance().getBackendUser().getUid())) {
            view = findViewById(R.id.preview);
        }
        FrameLayout container = findViewById(R.id.game_container);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();

        int width = dpToPx(55);
        layoutParams.leftToLeft = R.id.game_container;
        layoutParams.topToTop = R.id.game_container;
        layoutParams.leftMargin = (int) (container.getLeft() + model.position.x - width / 2.0);
        layoutParams.topMargin = (int) (container.getTop() + model.position.y- width / 2.0);
        layoutParams.width = width;
        layoutParams.height = width;

        view.setLayoutParams(layoutParams);
    }
});
```
