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
      [self presentViewController:imagePickerVc animated:YES completion:nil];
     
      result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else {
      result(FlutterMethodNotImplemented);
  }
}

- (void)imagePickerController:(TZImagePickerController *)picker didFinishPickingPhotos:(NSArray<UIImage *> *)photos sourceAssets:(NSArray *)assets isSelectOriginalPhoto:(BOOL)isSelectOriginalPhoto {
    _imagePickerVc.sortAscendingByModificationDate = NO;
    _imagePickerVc.photoWidth = 1024.0;
    _imagePickerVc.photoPreviewMaxWidth = 3072.0;
    [self presentViewController:imagePickerVc animated:YES completion:nil];
    
}
    

@end
