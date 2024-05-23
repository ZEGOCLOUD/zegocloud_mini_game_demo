#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@class SudGameInfo;

typedef void (^ISudListenerGetMGInfo)(int retCode, const NSString* retMsg, SudGameInfo* _Nullable gameInfo);

NS_ASSUME_NONNULL_END
