# README

This demo is a demonstration of combining audio with mini-games in game rooms, providing a reference for customers on how to increase entertainment for their applications.

## 1. Authentication Information Application
Before accessing mini-games, you need to register for our account on the [ZEGOCLOUD Console](https://console.zegocloud.com/).

And provide the following information for us to configure server authentication information:
**Application information:**
| Platform | iOS | Android | Example |
| --- |--- | --- | --- |
| ApplicationID/BundleID | | |com.zegocloud.minigame.demo |
| AppName | | | Mini-Game Demo |

**Server authentication URL：**

| Interface Name | Description | Test Environment | Formal Environment | Example |
| --- | --- | --- | --- | --- |
| get_sstoken | Get authentication token | | | https://zegocloud.com/get_sstoken |
| update_sstoken | Update authentication token | | | https://zegocloud.com/update_sstoken |
| get_user_info | Get user information | | | https://zegocloud.com/get_user_info |
| report_game_info | Report game data | | | https://zegocloud.com/report_game_info |
| notify | Game notification | | | https://zegocloud.com/notify |

You may refer to [the implementation document of the related interfaces](https://zegocloud.feishu.cn/docx/W1uDd7oBMoVY3AxnBvYcj8IMntc).

## 2. Run the Demo
### 2.1 Server Deployment
Mini-Game uses server authentication. Therefore, before running the demo, you need to deploy the related server services. You can download the server sample code from [here](https://github.com/ZEGOCLOUD/zegocloud-mini-game-server). After downloading, you need to set the authentication information in `config.js` and then deploy the related services.


> Please note that the interface URL deployed here needs to be consistent with the interface URL provided for obtaining authentication information.

## 3. Integration SDK
- [Android integration document](https://github.com/ZEGOCLOUD/zegocloud_mini_game_demo/blob/master/sud_mini_game_android/README.md)
- [iOS integration document](https://github.com/ZEGOCLOUD/zegocloud_mini_game_demo/blob/master/sud_mini_game_ios/README.md)
- [Server integration document](https://docs.sud.tech/en-US/app/Server/StartUp-Java.html)