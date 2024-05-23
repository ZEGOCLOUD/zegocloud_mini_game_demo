#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * Priority constant for the println method; use Log.v.
 */
#define SudLogVERBOSE  2

/**
 * Priority constant for the println method; use Log.d.
 */
#define SudLogDEBUG 3

/**
 * Priority constant for the println method; use Log.i.
 */
#define SudLogINFO 4

/**
 * Priority constant for the println method; use Log.w.
 */
#define SudLogWARN 5

/**
 * Priority constant for the println method; use Log.e.
 */
#define SudLogERROR 6

/**
 * Priority constant for the println method.
 */
#define SudLogASSERT 7

@protocol ISudLogger <NSObject>
- (void) setLogLevel:(int) level;
- (void) log:(int) level tag:(NSString*) tag msg:(NSString*) msg detailLine:(NSString *)detailLine;
- (void) log:(int) level tag:(NSString*) tag msg:(NSString*) msg error:(NSError *) error detailLine:(NSString *)detailLine;
@end

NS_ASSUME_NONNULL_END
