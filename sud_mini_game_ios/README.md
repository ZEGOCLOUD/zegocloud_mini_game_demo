# README

## 1. Integration Game SDK 
1. Add `SudMGPWrapper` dependency to your app's main Podfile.
    ([SDK latest version](https://github.com/SudTechnology/sud-mgp-ios/blob/main/README_en.md))
    ```
    pod 'SudMGPWrapper', '~> x.x.x'
    ```
  
2. Your business server needs to use [SudMGPAuth](https://docs.sud.tech/en-US/app/Server/StartUp-Java.html) to provide an API Which can request the authentication code. And update the `getCode_url` parameter on `GameViewController.swift` file.
    ```swift
    let getCode_url = = "Your server API";
    ```
3. Define a game view in the target ViewController. Such as `GameViewController`.
    ```swift
    @IBOutlet weak var gameView: UIView!
    ```
4. Implement the init SDK method
    ```swift
    private func initGame() {
        getCode(userId: userID) { code, error, retCode in
            self.initSudMGPSDK(gameID: self.gameID, roomID: self.roomID, userID: self.userID, code: code)
        } fail: { error in
            print("Login game error: \(error.code)")
        }
    }
    
    private func initSudMGPSDK(gameID: Int, roomID: String, userID: String, code: String) {
        // Ensure that no game is loaded before initialization, destroy SudMGP to guarantee it
        destroyGame()
        
        // Initialize SudMGP SDK
        let paramModel = SudInitSDKParamModel()
        paramModel?.appId = SUDMGP_APP_ID
        paramModel?.appKey = SUDMGP_APP_KEY
        paramModel?.isTestEnv = true
        
        SudMGP.initSDK(paramModel!) { retCode, retMsg in
            if retCode != 0 {
                print("initSudMGPSDK error: \(retCode) message: \(retMsg)")
                return
            }
            self.loadGame(gameID: gameID, roomID: roomID, userID: userID, code: code)
        }
    }
    
    private func destroyGame() {
        sudFSMMGDecorator.clearAllStates()
        sudFSTAPPDecorator.destroyMG()
    }
    ```
5. Implement load game method
    ```swift
    private func loadGame(gameID: Int, roomID: String, userID: String, code: String) {
        // Set current logged-in user
        self.sudFSMMGDecorator.setCurrentUserId(userID)
        
        // Load SudMGP SDK
        let paramModel = SudLoadMGParamModel()
        paramModel?.userId = userID
        paramModel?.roomId = roomID
        paramModel?.code = code
        paramModel?.mgId = gameID
        paramModel?.language = "en-US"
        paramModel?.gameViewContainer = self.gameView
        
        let iSudFSTAPP = SudMGP.loadMG(paramModel!, fsmMG: sudFSMMGDecorator)
        sudFSTAPPDecorator.iSudFSTAPP = iSudFSTAPP
    }
    ```
6. Implement load game and update status method
    ```swift
    private func joinGame() {
        let gameSettingInfo = AppCommonGameSettingGameInfo()
        gameSettingInfo.ludo.chessNum = 2
        gameSettingInfo.ludo.item = 1
        gameSettingInfo.ludo.mode = 0
        sudFSTAPPDecorator.notifyAppCommonGameSettingSelect(gameSettingInfo)
        sudFSTAPPDecorator.notifyAppComonSelf(in: true, seatIndex: -1, isSeatRandom: true, teamId: 1)
    }
    
    private func updateReadyStatus() {
        sudFSTAPPDecorator.notifyAppCommonSelfReady(true)
    }
    ```
7. Implement `SudFSMMGListener`
    ```swift
    extension GameViewController: SudFSMMGListener {
    func onGetGameViewInfo(_ handle: ISudFSMStateHandle, dataJson: String) {
        let scale = UIScreen.main.nativeScale
        // Screen size
        let screenSize = UIScreen.main.bounds.size
        // Safe area insets
        let safeArea = self.view.safeAreaInsets
        // Status bar height
        let statusBarHeight = safeArea.top == 0 ? 20 : safeArea.top
        
        let m = GameViewInfoModel()
        // Game display area
        m.view_size.width = screenSize.width * scale
        m.view_size.height = screenSize.height * scale
        // Game content layout safe area, adjust top margin based on your business needs
        // Top margin
        m.view_game_rect.top = (statusBarHeight + 80) * scale
        // Left
        m.view_game_rect.left = 0
        // Right
        m.view_game_rect.right = 0
        // Bottom safe area
        m.view_game_rect.bottom = (safeArea.bottom + 100) * scale
        
        m.ret_code = 0
        m.ret_msg = "success"
        handle.success(m.toJSON() ?? "")
    }
    
    func onExpireCode(_ handle: ISudFSMStateHandle, dataJson: String) {
        
    }
    
    func onGetGameCfg(_ handle: ISudFSMStateHandle, dataJson: String) {
        let model = GameCfgModel.default()
        handle.success(model.toJSON() ?? "")
        
    }
    
    func onGameMGCommonGameCreateOrder(_ handle: ISudFSMStateHandle, model: MgCommonGameCreateOrderModel) {
        let model = AppCommonGameCreateOrderResult()
        model.result = 1
        sudFSTAPPDecorator.notify(model)
    }
    
    func onGameLoadingProgress(_ stage: Int32, retCode: Int32, progress: Int32) {
        if progress == 100 {
            micButton.isHidden = false
            startPublish()
            joinGame()
            updateReadyStatus()
        }
    }
}
    ```

## 2. SudMGP SDK Introduction
### 2.1 SudMGP Client SDK
- [Android SDK](https://github.com/SudTechnology/sud-mgp-android/blob/main/README_en.md)
- [iOS SDK](https://github.com/SudTechnology/sud-mgp-ios/blob/main/README_en.md)
### 2.2 SudMGPWrapper
- `SudMGPWrapper` encapsulates SudMGP and simplifies the interaction between the app and the game.
- `SudMGPWrapper` is continuously maintained and kept up to date.
- It is recommended that app developers use `SudMGPWrapper`.
- Core classes of `SudMGPWrapper` include `SudMGPAPPState`, `SudMGPMGState`, `SudFSMMGListener`, `SudFSMMGDecorator`, and `SudFSTAPPDecorator`.

### 2.3 App Calling the Game 

-  `SudMGPAPPState`  encapsulates [App Common State](https://docs.sud.tech/en-US/app/Client/APPFST/CommonState.html). 
-  `SudFSTAPPDecorator`  encapsulates [ISudFSTAPP](https://docs.sud.tech/en-US/app/Client/API/ISudFSTAPP.html) interfaces, including [notifyStateChange](https://docs.sud.tech/app/Client/APPFST/CommonState.html) and foo. 
-  `SudFSTAPPDecorator`  is responsible for encapsulating each App common state into an interface.  
    ```objc
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
-  `SudMGPMGState` encapsulates [Common State - Game](https://docs.sud.tech/en/app/Client/MGFSM/CommonStateGame.html) and [Common State - Player](https://docs.sud.tech/en/app/Client/MGFSM/CommonStatePlayer.html);
-  `SudFSMMGListener` encapsulates three types of [ISudFSMMG](https://docs.sud.tech/en/app/Client/API/ISudFSMMG.html) callback functions, onGameStateChange, onPlayerStateChange, and onFoo;
-  `SudFSMMGListener` is responsible for encapsulating each game state into a separate callback function;

`SudFSMMGListener` class
```objc
@protocol SudFSMMGListener <NSObject>
    
    @required
    /// Get game view information  【Need to implement】
    - (void)onGetGameViewInfo:(nonnull id<ISudFSMStateHandle>)handle dataJson:(nonnull NSString *)dataJson;
    
    /// Short-term token code expires  【Need to implement】
    - (void)onExpireCode:(nonnull id<ISudFSMStateHandle>)handle dataJson:(nonnull NSString *)dataJson;
    
    /// Get game config  【Need to implement】
    - (void)onGetGameCfg:(nonnull id<ISudFSMStateHandle>)handle dataJson:(nonnull NSString *)dataJson;
    
    
    @optional
    /// Game starts
    - (void)onGameStarted;
    
    /// Game destroyed
    - (void)onGameDestroyed;
    
    /// Common state - game
    /// Game: Public screen message state    MG_COMMON_PUBLIC_MESSAGE
    - (void)onGameMGCommonPublicMessage:(nonnull id<ISudFSMStateHandle>)handle model:(MGCommonPublicMessageModel *)model;
    ...
@end
```
    
`SudFSMMGDecorator` class
    
```objc
/// game -> app
@interface SudFSMMGDecorator : NSObject <ISudFSMMG>
  
    typedef NS_ENUM(NSInteger, GameStateType) {
        /// Idle
        GameStateTypeLeisure = 0,
        /// loading
        GameStateTypeLoading = 1,
        /// playing
        GameStateTypePlaying = 2,
    };
  
    /// Current user ID
    @property(nonatomic, strong, readonly)NSString *currentUserId;
    // Game state enumeration: GameStateType
    @property (nonatomic, assign) GameStateType gameStateType;
    /// Whether the current user has joined
    @property (nonatomic, assign) BOOL isInGame;
    /// Whether it is in the game
    @property (nonatomic, assign) BOOL isPlaying;
  
    ...
  
    /// Set event handler
    /// @param listener Event handling instance
    - (void)setEventListener:(id<SudFSMMGListener>)listener;
    /// Set the current user ID
    /// @param userId Current user ID
    - (void)setCurrentUserId:(NSString *)userId;
    /// Clear all stored arrays
    - (void)clearAllStates;
    /// 2MG success callback
    - (NSString *)handleMGSuccess;
    /// 2MG failure callback
    - (NSString *)handleMGFailure;
  
    #pragma mark - Get the latest state in gamePlayerStateMap
    /// Get user join status
    - (BOOL)isPlayerIn:(NSString *)userId;
    /// Whether the user is in preparation
    - (BOOL)isPlayerIsReady:(NSString *)userId;
    /// Whether the user is in the game
    - (BOOL)isPlayerIsPlaying:(NSString *)userId;
    /// Whether the user is the captain
    - (BOOL)isPlayerIsCaptain:(NSString *)userId;
    /// Whether the user is painting
    - (BOOL)isPlayerPaining:(NSString *)userId;
  
    #pragma mark - Check if it exists in gamePlayerStateMap (used to determine if the user is in the game)
    /// Whether the user has joined the game
    - (BOOL)isPlayerInGame:(NSString *)userId;
@end
```