//
//  MediaInfo.h
//  GPUImage
//
//  Created by 那晓光 on 2018/8/7.
//

#import <Foundation/Foundation.h>

@interface MediaInfo : NSObject

@property (nonatomic ,assign) NSUInteger width ;
@property (nonatomic ,assign) NSUInteger height ;
@property (nonatomic ,assign) NSUInteger duration ;
@property (nonatomic ,assign) NSString*  mimeType ;
@property (nonatomic ,assign) double createTime ;
@property (nonatomic ,assign) double latitude ;
@property (nonatomic ,assign) double longitude ;

@end

