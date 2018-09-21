#import "MediaPickerPlugin.h"
#import "TZImagePickerController.h"
#import "MediaInfo.h"

@interface MediaPickerPlugin ()<TZImagePickerControllerDelegate>
    @property (nonatomic, retain) TZImagePickerController *imagePickerVc;
@end

@implementation MediaPickerPlugin

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"media_picker"
            binaryMessenger:[registrar messenger]];
  MediaPickerPlugin* instance = [[MediaPickerPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"pick" isEqualToString:call.method]) {
      _imagePickerVc = [[TZImagePickerController alloc] initWithMaxImagesCount:9 delegate:self];
     
      // You can get the photos by block, the same as by delegate.
      // 你可以通过block或者代理，来得到用户选择的照片.
      _imagePickerVc.allowPickingOriginalPhoto = YES;
      _imagePickerVc.isSelectOriginalPhoto = YES;
      _imagePickerVc.sortAscendingByModificationDate = NO;
    
      
      UIViewController *viewController = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
      if ( viewController.presentedViewController && !viewController.presentedViewController.isBeingDismissed ) {
          viewController = viewController.presentedViewController;
      }
      
    
      [_imagePickerVc setDidFinishPickingPhotosHandle:^(NSArray<UIImage *> *photos, NSArray *assets, BOOL isSelectOriginalPhoto) {
          
          NSArray<MediaInfo *> *mediaInfoArray = @[] ;
          for (PHAsset *asset in assets) {
              CLLocation *location = [asset location] ;
              PHAssetMediaType mediaType = [asset mediaType];
              
              NSDate *creationDate = [asset creationDate];
              double timeInterval = [creationDate timeIntervalSince1970] ;
              
              MediaInfo *mediaInfo = [MediaInfo alloc] ;
              mediaInfo.width = [asset pixelWidth] ;
              mediaInfo.height = [asset pixelHeight] ;
              mediaInfo.duration = [asset duration] ;
              mediaInfo.createTime = timeInterval ;
              if(mediaType == PHAssetMediaTypeImage){
                  mediaInfo.mimeType = @"image/*" ;
              }else if(mediaType == PHAssetMediaTypeVideo){
                  mediaInfo.mimeType = @"video/*" ;
              }else if(mediaType == PHAssetMediaTypeAudio){
                  mediaInfo.mimeType = @"audio/*" ;
              }
              mediaInfo.latitude = [location horizontalAccuracy] ;
              mediaInfo.longitude = [location verticalAccuracy] ;
              [mediaInfoArray arrayByAddingObject:mediaInfo];
          };
          
          NSError *error = nil;
          NSData *jsonData = [NSJSONSerialization dataWithJSONObject:mediaInfoArray options:kNilOptions error:&error];
          NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
          result(jsonString) ;
         
      }];
      
      [viewController presentViewController:_imagePickerVc animated:YES completion:nil];
     
      result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else {
      result(FlutterMethodNotImplemented);
  }
}


@end
