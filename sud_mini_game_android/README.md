# README

## 1. Integration Game SDK 
1. Reference demo, use Android Studio to import the `SudMGPWrapper` module. 
2. Add the `SudMGPWrapper` dependency in the `/app/build.gradle` file.
  ``` java
  dependencies {
     // Import SudMGPWrapper
     implementation project(':SudMGPWrapper')
  }
  ```
3. Copy the following 3 files to the project: 
    - BaseGameViewModel.java
    - QuickStartGameViewModel.java
    - QuickStartUtils.java
4. Update the `SudMGP_APP_ID` and `SudMGP_APP_KEY` parameters on `QuickStartGameViewModel.java` file. 
  ```java
  /** appId obtained from the Sud platform */
  public static String SudMGP_APP_ID = "Your app ID";
  /** appKey obtained from the Sud platform */
  public static String SudMGP_APP_KEY = "Your app key";
  public static final boolean GAME_IS_TEST_ENV = true;
  ```
5. Your business server needs to use [SudMGPAuth](https://docs.sud.tech/en-US/app/Server/StartUp-Java.html) to provide an API Which can request the authentication code. And update the `getCode_url` parameter on `QuickStartGameViewModel.java` file.
    ```java
    public static String getCode_url = "Your server API";
    ```
6. Define a game view container in the layout file, for example: `activity_game.xml` 

    ``` xml
    <!-- Game view container, the android:visibility property should not be set to gone -->
    <FrameLayout
        android:id="@+id/game_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    ``` 
7. Create an instance of `QuickStartGameViewModel` and map it to the corresponding lifecycle.
    ```java
    private final QuickStartGameViewModel gameViewModel = new QuickStartGameViewModel();
    ```   
8. Implement adding and removing the game view. 

    ``` java
    private final QuickStartGameViewModel gameViewModel = new QuickStartGameViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        long gameID = intent.getLongExtra("gameID", 1468180338417074177L);

        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameViewModel.gameViewLiveData.observe(this, new Observer<View>() {
            @Override
            public void onChanged(View view) {
                if (view == null) {
                    gameContainer.removeAllViews();
                } else {
                    gameContainer.addView(view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

                    SudMGPAPPState.Ludo ludo = new SudMGPAPPState.Ludo();
                    ludo.chessNum = 2;
                    ludo.item = 1;
                    ludo.mode =0;
                    gameViewModel.sudFSTAPPDecorator.notifyAPPCommonGameSettingSelectInfo(ludo);
                    gameViewModel.sudFSTAPPDecorator.notifyAPPCommonSelfIn(true, -1, true, 1);
                    gameViewModel.sudFSTAPPDecorator.notifyAPPCommonSelfReady(true);
                }
            }
        });

        joinGame(gameID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameViewModel.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameViewModel.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameViewModel.onDestroy();
        finish();
    }
    ```
9. Join the game room

    ``` java
    private  void joinGame(long gameID) {
        String appRoomId = roomID;
        long mgId = gameID;
        gameViewModel.switchGame(this, appRoomId, mgId);
    }    
    ```
10. Destroy the game 

    ``` java
    // Destroy the game before the page is destroyed
    gameViewModel.destroyMG();
    finish(); 
    ```
## 2. SudMGP SDK Introduction
### 2.1 SudMGP Client SDK
- [Android SDK](https://github.com/SudTechnology/sud-mgp-android/blob/main/README_en.md)
- [iOS SDK](https://github.com/SudTechnology/sud-mgp-ios/blob/main/README_en.md)
### 2.2 SudMGPWrapper
- `SudMGPWrapper` encapsulates SudMGP and simplifies the interaction between the app and the game.
- `SudMGPWrapper` is continuously maintained and kept up to date.
- It is recommended that app developers to use `SudMGPWrapper`.
- Core classes of `SudMGPWrapper` include `SudMGPAPPState`, `SudMGPMGState`, `SudFSMMGListener`, `SudFSMMGDecorator`, and `SudFSTAPPDecorator`.

### 2.3 App Calling the Game 

-  `SudMGPAPPState`  encapsulates [App Common State](https://docs.sud.tech/en-US/app/Client/APPFST/CommonState.html). 
-  `SudFSTAPPDecorator`  encapsulates [ISudFSTAPP](https://docs.sud.tech/en-US/app/Client/API/ISudFSTAPP.html) interfaces, including [notifyStateChange](https://docs.sud.tech/app/Client/APPFST/CommonState.html) and foo. 
-  `SudFSTAPPDecorator`  is responsible for encapsulating each App common state into an interface.  
Here is a code framework for the  SudFSTAPPDecorator  class:

    ``` java
    public class SudFSTAPPDecorator {
        // iSudFSTAPP = SudMGP.loadMG(QuickStartActivity, userId, roomId, code, gameId, language, sudFSMMGDecorator);
        public void setISudFSTAPP(ISudFSTAPP iSudFSTAPP);
        // 1. Join state
        public void notifyAPPCommonSelfIn(boolean isIn, int seatIndex, boolean isSeatRandom, int teamId);
        ...
        // 16. Set AI players in the game (added on 2022-05-11)
        public void notifyAPPCommonGameAddAIPlayers(List<SudMGPAPPState.AIPlayers> aiPlayers, int isReady);
        public void startMG();
        public void pauseMG();
        public void playMG();
        public void stopMG();
        public void destroyMG();
        public void updateCode(String code, ISudListenerNotifyStateChange listener);
        public void pushAudio(ByteBuffer buffer, int bufferLength);
        ...
    }
    ```

### 2.4 Game Calling the App 
-  `SudMGPMGState`  encapsulates [Common State - Game](https://docs.sud.tech/en-US/app/Client/MGFSM/CommonStateGame.html) and [Common State - Player](https://docs.sud.tech/en-US/app/Client/MGFSM/CommonStatePlayer.html). 
-  `SudFSMMGListener`  encapsulates three types of callback functions from [ISudFSMMG](https://docs.sud.tech/en-US/app/Client/API/ISudFSMMG.html): onGameStateChange, onPlayerStateChange, and onFoo. 
-  `SudFSMMGListener`  is responsible for encapsulating each game state into separate callback functions. 

    ```java
    public interface SudFSMMGListener {
    default void onGameLog(String str) {}
    void onGameStarted();
    void onGameDestroyed();
    void onExpireCode(ISudFSMStateHandle handle, String dataJson);
    void onGetGameViewInfo(ISudFSMStateHandle handle, String dataJson);
    void onGetGameCfg(ISudFSMStateHandle handle, String dataJson);
    // Common State - Game
    // void onGameStateChange(ISudFSMStateHandle handle, String state, String dataJson);
    // Documentation: [Common State - Game](https://docs.sud.tech/app/Client/MGFSM/CommonStateGame.html)
    // 1. Game common public message
    default void onGameMGCommonPublicMessage(ISudFSMStateHandle handle, SudMGPMGState.MGCommonPublicMessage model);
    ...
    // 21. Game notifies the app layer whether adding AI players is successful (added on 2022-05-17)
    default void onGameMGCommonGameAddAIPlayers(ISudFSMStateHandle handle, SudMGPMGState.MGCommonGameAddAIPlayers model);
    // Common State - Player
    // void onPlayerStateChange(ISudFSMStateHandle handle, String userId, String state, String dataJson);
    // Documentation: [Common State - Player](https://docs.sud.tech/app/Client/MGFSM/CommonStatePlayer.html)
    // 1. Player join state
    default void onPlayerMGCommonPlayerIn(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonPlayerIn model);
    ...
    // 11. Game notifies the app layer of the remaining game time (added on 2022-05-23, currently effective for UMO)
    default void onPlayerMGCommonGameCountdownTime(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGCommonGameCountdownTime model);
    // Game-specific state: Draw Guess
    // Documentation: [Draw Guess](https://docs.sud.tech/app/Client/MGFSM/DrawGuess.html)
    // 1. Selecting word state
    default void onPlayerMGDGSelecting(ISudFSMStateHandle handle, String userId, SudMGPMGState.MGDGSelecting model);
    ...
    }
    ```

- The decorator class  `SudFSMMGDecorator`  for [ISudFSMMG](https://docs.sud.tech/en-US/app/Client/API/ISudFSMMG.html)
    ``` java
    public class SudFSMMGDecorator implements ISudFSMMG {
    // Set the callback
    public void setSudFSMMGListener(SudFSMMGListener listener);
    // Game log
    public void onGameLog(String dataJson);
    // Game loading progress
    public void onGameLoadingProgress(int stage, int retCode, int progress);
    // Game has started, game long connection is complete
    public void onGameStarted();
    // Game destroyed
    public void onGameDestroyed();
    // Code expired, must be implemented; APP integrators must call handle.success to release the asynchronous callback object
    public void onExpireCode(ISudFSMStateHandle handle, String dataJson);
    // Get game view information, must be implemented; APP integrators must call handle.success to release the asynchronous callback object
    // GameViewInfoModel documentation: [link](https://docs.sud.tech/app/Client/API/ISudFSMMG/onGetGameViewInfo.html)
    public void onGetGameViewInfo(ISudFSMStateHandle handle, String dataJson);
    // Get game config, must be implemented; APP integrators must call handle.success to release the asynchronous callback object
    // GameConfigModel documentation: [link](https://docs.sud.tech/app/Client/API/ISudFSMMG/onGetGameCfg.html)
    public void onGetGameCfg(ISudFSMStateHandle handle, String dataJson);
    // Game state change; APP integrators must call handle.success to release the asynchronous callback object
    public void onGameStateChange(ISudFSMStateHandle handle, String state, String dataJson);
    // Player state change, APP integrators must call handle.success to release the asynchronous callback object
    public void onPlayerStateChange(ISudFSMStateHandle handle, String userId, String state, String dataJson);
    // ...
    }
    ```
