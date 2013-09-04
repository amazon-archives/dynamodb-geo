/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

#import "AWSDetailViewController.h"

#import "AWSConstants.h"
#import "AWSGeoMapViewController.h"

@interface AWSDetailViewController ()

@property (nonatomic, strong) NSMutableData *data;

@end

@implementation AWSDetailViewController

#pragma mark - Class lifecycle methods

- (void)viewDidLoad
{
    [super viewDidLoad];

    [self registerForKeyboardNotifications];
    [self getPoint];
}

#pragma mark - Main methods

- (IBAction)updateButtonPressed:(id)sender {
    NSDictionary *requestDictionary = @{@"action" : @"update-point",
                                        @"request" : @{
                                                @"lat" : [NSNumber numberWithDouble:self.coordinate.latitude],
                                                @"lng" : [NSNumber numberWithDouble:self.coordinate.longitude],
                                                @"rangeKey" : self.rangeKey,
                                                @"schoolName" : self.schoolNameTextField.text,
                                                @"memo" : self.memoTextField.text
                                                }
                                        };
    [self sendRequest:requestDictionary];
}

- (IBAction)deleteButtonPressed:(id)sender {
    NSDictionary *requestDictionary = @{@"action" : @"delete-point",
                                        @"request" : @{
                                                @"lat" : [NSNumber numberWithDouble:self.coordinate.latitude],
                                                @"lng" : [NSNumber numberWithDouble:self.coordinate.longitude],
                                                @"rangeKey" : self.rangeKey
                                                }
                                        };
    [self sendRequest:requestDictionary];
}

- (void)getPoint {
    NSDictionary *requestDictionary = @{@"action" : @"get-point",
                                        @"request" : @{
                                                @"lat" : [NSNumber numberWithDouble:self.coordinate.latitude],
                                                @"lng" : [NSNumber numberWithDouble:self.coordinate.longitude],
                                                @"rangeKey" : self.rangeKey
                                                }
                                        };
    [self sendRequest:requestDictionary];
}

- (void)sendRequest:(NSDictionary *)requestDictionary {
    NSLog(@"Request:\n%@", requestDictionary);

    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:AWSElasticBeanstalkEndpoint]
                                                           cachePolicy:NSURLRequestUseProtocolCachePolicy
                                                       timeoutInterval:120.0];
    request.HTTPBody = [NSJSONSerialization dataWithJSONObject:requestDictionary
                                                       options:kNilOptions
                                                         error:nil];
    request.HTTPMethod = @"POST";
    NSURLConnection *conn = [[NSURLConnection alloc] initWithRequest:request delegate:self];
    if (conn) {
        self.data = [NSMutableData data];
    }
}

- (void)reloadMapView {
    NSArray *viewControllers = self.navigationController.viewControllers;
    AWSGeoMapViewController *mapViewController = [viewControllers objectAtIndex:[viewControllers count] - 2];
    [mapViewController queryArea];
}

#pragma mark - NSURLConnection delegate methods

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
    [self.data appendData:data];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection {
    NSDictionary *resultDictionary = [NSJSONSerialization JSONObjectWithData:self.data
                                                                     options:kNilOptions
                                                                       error:nil];
    NSLog(@"Result:\n%@", resultDictionary);

    NSString *action = [resultDictionary objectForKey:@"action"];
    if([action isEqualToString:@"get-point"]) {
        NSDictionary *itemDictionary = [resultDictionary objectForKey:@"result"];

        self.latitudeTextField.text = [itemDictionary objectForKey:@"latitude"];
        self.longitudeTextField.text = [itemDictionary objectForKey:@"longitude"];
        self.hashKeyTextField.text = [itemDictionary objectForKey:@"hashKey"];
        self.rangeKeyTextField.text = [itemDictionary objectForKey:@"rangeKey"];
        self.geohashTextField.text = [itemDictionary objectForKey:@"geohash"];
        self.schoolNameTextField.text = [itemDictionary objectForKey:@"schoolName"];
        self.memoTextField.text = [itemDictionary objectForKey:@"memo"];
    } else if([action isEqualToString:@"update-point"]) {
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Succeeded"
                                                            message:@"The record was successfully updated."
                                                           delegate:nil
                                                  cancelButtonTitle:@"OK"
                                                  otherButtonTitles:nil];
        [alertView show];

        [self reloadMapView];
    } else if([action isEqualToString:@"delete-point"]) {
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Succeeded"
                                                            message:@"The record was successfully deleted."
                                                           delegate:nil
                                                  cancelButtonTitle:@"OK"
                                                  otherButtonTitles:nil];
        [alertView show];

        [self reloadMapView];
        [self.navigationController popViewControllerAnimated:YES];
    }
}

#pragma mark - Keyboard management methods

- (void)registerForKeyboardNotifications {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWasShown:)
                                                 name:UIKeyboardDidShowNotification object:nil];

    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWillBeHidden:)
                                                 name:UIKeyboardWillHideNotification object:nil];
}

- (void)keyboardWasShown:(NSNotification*)aNotification {
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;

    UIEdgeInsets contentInsets = UIEdgeInsetsMake(0.0, 0.0, kbSize.height, 0.0);
    self.scrollView.contentInset = contentInsets;
    self.scrollView.scrollIndicatorInsets = contentInsets;

    [self.scrollView scrollRectToVisible:self.updateButton.frame animated:YES];
}

- (void)keyboardWillBeHidden:(NSNotification*)aNotification
{
    UIEdgeInsets contentInsets = UIEdgeInsetsZero;
    self.scrollView.contentInset = contentInsets;
    self.scrollView.scrollIndicatorInsets = contentInsets;
}

@end
