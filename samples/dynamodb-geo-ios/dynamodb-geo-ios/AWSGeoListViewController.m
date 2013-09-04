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

#import "AWSGeoListViewController.h"

#import <CoreLocation/CoreLocation.h>
#import "AWSPointAnnotation.h"

@interface AWSGeoListViewController ()



@end

@implementation AWSGeoListViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    self.title = @"List View";

    self.annotations = [self.annotations sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        AWSPointAnnotation *annotation1 = (AWSPointAnnotation *)obj1;
        AWSPointAnnotation *annotation2 = (AWSPointAnnotation *)obj2;

        CLLocation *location1 = [[CLLocation alloc] initWithLatitude:annotation1.coordinate.latitude
                                                           longitude:annotation1.coordinate.longitude];
        CLLocation *location2 = [[CLLocation alloc] initWithLatitude:annotation2.coordinate.latitude
                                                           longitude:annotation2.coordinate.longitude];

        NSNumber *distance1 = [NSNumber numberWithDouble:[self.center distanceFromLocation:location1]];
        NSNumber *distance2 = [NSNumber numberWithDouble:[self.center distanceFromLocation:location2]];

        return [distance1 compare:distance2];
    }];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.annotations count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = @"Cell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
    }

    AWSPointAnnotation *annotation = [self.annotations objectAtIndex:indexPath.row];
    CLLocation *location = [[CLLocation alloc] initWithLatitude:annotation.coordinate.latitude
                                                      longitude:annotation.coordinate.longitude];
    cell.textLabel.text = annotation.title;
    cell.detailTextLabel.text = [NSString stringWithFormat:@"%.2f ft from the center",
                                 ([self.center distanceFromLocation:location] * 3.28084)];

    return cell;
}

@end
