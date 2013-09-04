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

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>

@interface AWSDetailViewController : UIViewController
{}

@property (nonatomic, strong) NSString *rangeKey;
@property (nonatomic, assign) CLLocationCoordinate2D coordinate;

@property (nonatomic, strong) IBOutlet UIScrollView *scrollView;
@property (nonatomic, strong) IBOutlet UITextField *latitudeTextField;
@property (nonatomic, strong) IBOutlet UITextField *longitudeTextField;
@property (nonatomic, strong) IBOutlet UITextField *hashKeyTextField;
@property (nonatomic, strong) IBOutlet UITextField *rangeKeyTextField;
@property (nonatomic, strong) IBOutlet UITextField *geohashTextField;
@property (nonatomic, strong) IBOutlet UITextField *schoolNameTextField;
@property (nonatomic, strong) IBOutlet UITextField *memoTextField;
@property (nonatomic, strong) IBOutlet UIButton *updateButton;
@property (nonatomic, strong) IBOutlet UIButton *deleteButton;

- (IBAction)updateButtonPressed:(id)sender;
- (IBAction)deleteButtonPressed:(id)sender;

@end
