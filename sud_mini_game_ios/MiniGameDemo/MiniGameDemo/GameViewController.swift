//
//  GameViewController.swift
//  MiniGameDemo
//
//  Created by Larry on 2024/1/22.
//

import UIKit
import SudMGP
import SudMGPWrapper
import ZegoExpressEngine

class GameViewController: UIViewController {
    let getCode_url = "Your get code URL"; // Connect ZEGOCLOUD Technical support to get
    let SUDMGP_APP_ID = "Your Game App ID" // Connect ZEGOCLOUD Technical support to get
    let SUDMGP_APP_KEY = "Your Game App Key" // Connect ZEGOCLOUD Technical support to get
    let gameID = 1468180338417074177
    let roomID = "room_111"
    let userID = "111_\(Date.now.timeIntervalSince1970)"
    
    let appID: UInt32 = Your App ID // Get from ZEGOCLOUD console https://console.zegocloud.com/
    let appSign = "Your App Sign" // Get from ZEGOCLOUD console https://console.zegocloud.com/
    
    let sudFSTAPPDecorator = SudFSTAPPDecorator()
    let sudFSMMGDecorator = SudFSMMGDecorator()
    
    
    @IBOutlet weak var gameView: UIView!
    @IBOutlet weak var micButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        micButton.isHidden = true
        micButton.setImage(UIImage(named: "call_mic_close"), for: .selected)
        micButton.setImage(UIImage(named: "call_mic_open"), for: .normal)
        
        sudFSMMGDecorator.setEventListener(self)
        initGame()
        
        createEngine()
        loginRoom()
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        destroyGame()
        destroyEngine()
    }
    
    @IBAction func pressMicButton(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        ZegoExpressEngine.shared().muteMicrophone(sender.isSelected)
    }
    
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
    
    private func createEngine() {
        let profile = ZegoEngineProfile()
        
        // Get your AppID and AppSign from ZEGOCLOUD Console
        //[My Projects -> AppID] : https://console.zegocloud.com/project
        profile.appID = appID
        profile.appSign = appSign
        // Use the default scenario.
        profile.scenario = .default
        // Create a ZegoExpressEngine instance and set eventHandler to [self].
        ZegoExpressEngine.createEngine(with: profile, eventHandler: self)
    }
    
    private func destroyEngine() {
        ZegoExpressEngine.destroy(nil)
    }
    
    private func loginRoom() {
        // The value of `userID` is generated locally and must be globally unique.
        let user = ZegoUser(userID: userID)
        // The value of `roomID` is generated locally and must be globally unique.
        // Users must log in to the same room to call each other.
        let roomID = roomID
        let roomConfig = ZegoRoomConfig()
        // onRoomUserUpdate callback can be received when "isUserStatusNotify" parameter value is "true".
        roomConfig.isUserStatusNotify = true
        // log in to a room
        ZegoExpressEngine.shared().loginRoom(roomID, user: user, config: roomConfig) { errorCode, extendedData in
            if errorCode == 0 {
                // Login room successful
            } else {
                // Login room failed
            }
        }
    }
    
    private func logoutRoom() {
        ZegoExpressEngine.shared().logoutRoom()
    }
    
    private func startPublish() {
        // After calling the `loginRoom` method, call this method to publish streams.
        // The StreamID must be unique in the room.
        let streamID = "stream_" + userID
        ZegoExpressEngine.shared().enableCamera(false)
        ZegoExpressEngine.shared().startPublishingStream(streamID)
    }
    
    private func stopPublish() {
        ZegoExpressEngine.shared().stopPublishingStream()
    }
    
    private func startPlayStream(streamID: String) {
        ZegoExpressEngine.shared().startPlayingStream(streamID)
    }
    
    private func stopPlayStream(streamID: String) {
        ZegoExpressEngine.shared().stopPlayingStream(streamID)
    }
}

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

extension GameViewController : ZegoEventHandler {
    
    // Callback for updates on the status of the streams in the room.
    func onRoomStreamUpdate(_ updateType: ZegoUpdateType, streamList: [ZegoStream], extendedData: [AnyHashable : Any]?, roomID: String) {
        // If users want to play the streams published by other users in the room, call the startPlayingStream method with the corresponding streamID obtained from the `streamList` parameter where ZegoUpdateType == ZegoUpdateTypeAdd.
        if updateType == .add {
            for stream in streamList {
                startPlayStream(streamID: stream.streamID)
            }
        } else {
            for stream in streamList {
                stopPlayStream(streamID: stream.streamID)
            }
        }
    }
    
    // Callback for updates on the current user's room connection status.
    func onRoomStateUpdate(_ state: ZegoRoomState, errorCode: Int32, extendedData: [AnyHashable : Any]?, roomID: String) {
    }
    
    // Callback for updates on the status of other users in the room.
    // Users can only receive callbacks when the isUserStatusNotify property of ZegoRoomConfig is set to `true` when logging in to the room (loginRoom).
    func onRoomUserUpdate(_ updateType: ZegoUpdateType, userList: [ZegoUser], roomID: String) {
    }
}

// Network
extension GameViewController {
    
    private func getCode(userId: String, success: @escaping (String, NSError?, Int) -> Void, fail: @escaping (NSError) -> Void) {
        
        let dicParam: [String: Any] = ["uid": userId]
        getHttpRequest(withURL: getCode_url, param: dicParam, success: { rootDict in
            if let dic = rootDict["data"] as? [String: Any] {
                let code = dic["code"] as? String ?? ""
                let retCode = dic["ret_code"] as? Int ?? 0
                success(code, nil, retCode)
            }
        }) { error in
            print("login game server error: \(error.localizedDescription)")
            fail(error as NSError)
        }
    }
    
    private func postHttpRequest(withURL api: String, param: [String: Any]?, success: @escaping ([String: Any]) -> Void, failure: @escaping (Error) -> Void) {
        // Create URL
        guard let url = URL(string: api) else {
            let error = NSError(domain: "", code: -1, userInfo: [NSDebugDescriptionErrorKey: "Invalid URL"])
            failure(error)
            return
        }
        
        // Create request
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        // Set request body
        if let param = param {
            do {
                let bodyData = try JSONSerialization.data(withJSONObject: param, options: [])
                request.httpBody = bodyData
            } catch {
                failure(error)
                return
            }
        }
        
        // Create URLSession
        let session = URLSession.shared
        
        // Send network request
        let dataTask = session.dataTask(with: request) { (data, response, error) in
            DispatchQueue.main.async {
                if let error = error {
                    failure(error)
                    return
                }
                
                guard let data = data else {
                    let error = NSError(domain: "", code: -1, userInfo: [NSDebugDescriptionErrorKey: "No data received"])
                    failure(error)
                    return
                }
                
                do {
                    if let responseObject = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] {
                        success(responseObject)
                    } else {
                        let error = NSError(domain: "", code: -1, userInfo: [NSDebugDescriptionErrorKey: "Invalid JSON response"])
                        failure(error)
                    }
                } catch {
                    failure(error)
                }
            }
        }
        
        // Start the request
        dataTask.resume()
    }
    
    private func getHttpRequest(withURL api: String, param: [String: Any]?, success: @escaping ([String: Any]) -> Void, failure: @escaping (Error) -> Void) {
        // Create URL
        guard let url = URL(string: api) else {
            let error = NSError(domain: "", code: -1, userInfo: [NSDebugDescriptionErrorKey: "Invalid URL"])
            failure(error)
            return
        }
        var urlComponents = URLComponents(url: url, resolvingAgainstBaseURL: false)!
        
        if let param = param {
            var items = [URLQueryItem]()
            for key in param.keys {
                let item = URLQueryItem(name: key, value: param[key] as? String ?? "")
                items.append(item)
            }
            urlComponents.queryItems = items
        }
        
        guard let url = urlComponents.url else {
            print("Invalid URL components")
            return
        }
        
        
        // Create request
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        // Create URLSession
        let session = URLSession.shared
        
        // Send network request
        let dataTask = session.dataTask(with: request) { (data, response, error) in
            DispatchQueue.main.async {
                if let error = error {
                    failure(error)
                    return
                }
                
                guard let data = data else {
                    let error = NSError(domain: "", code: -1, userInfo: [NSDebugDescriptionErrorKey: "No data received"])
                    failure(error)
                    return
                }
                
                do {
                    if let responseObject = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] {
                        success(responseObject)
                    } else {
                        let error = NSError(domain: "", code: -1, userInfo: [NSDebugDescriptionErrorKey: "Invalid JSON response"])
                        failure(error)
                    }
                } catch {
                    failure(error)
                }
            }
        }
        
        // Start the request
        dataTask.resume()
    }
}
