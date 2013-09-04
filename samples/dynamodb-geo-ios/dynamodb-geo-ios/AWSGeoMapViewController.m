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

#import "AWSGeoMapViewController.h"

#import "AWSGeoListViewController.h"
#import "AWSPointAnnotation.h"
#import "AWSDetailViewController.h"
#import "AWSConstants.h"

@interface AWSGeoMapViewController ()

@property (nonatomic, strong) MKMapView *mapView;
@property (nonatomic, assign) BOOL radiusQueryEnabled;
@property (nonatomic, strong) NSMutableData *data;

@end

double const AWSRadiusInMeter = 1000;

@implementation AWSGeoMapViewController

#pragma mark - Class lifecycle methods

- (void)viewDidLoad
{
    [super viewDidLoad];

    self.title = @"School Locations";
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction
                                                                                           target:self
                                                                                           action:@selector(showActionSheet)];

    self.mapView = [MKMapView new];
    self.mapView.frame = self.parentViewController.view.frame;
    self.mapView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    self.mapView.region = MKCoordinateRegionMake(CLLocationCoordinate2DMake(47.61121, -122.31846),
                                                 MKCoordinateSpanMake(0.05, 0.05));
    self.mapView.delegate = self;

    self.radiusQueryEnabled = NO;

    [self.view addSubview:self.mapView];
}

#pragma mark - Main methods

- (void)queryArea {
    double latitude = self.mapView.region.center.latitude;
    double longitude = self.mapView.region.center.longitude;

    NSDictionary *requestDictionary = nil;
    if(self.radiusQueryEnabled == NO) {
        double latitudeDelta = self.mapView.region.span.latitudeDelta;
        double longitudeDelta = self.mapView.region.span.longitudeDelta;

        requestDictionary = @{@"action" : @"query-rectangle",
                              @"request" : @{
                                      @"minLat" : [NSNumber numberWithDouble:latitude - latitudeDelta / 2],
                                      @"minLng" : [NSNumber numberWithDouble:longitude - longitudeDelta / 2],
                                      @"maxLat" : [NSNumber numberWithDouble:latitude + latitudeDelta / 2],
                                      @"maxLng" : [NSNumber numberWithDouble:longitude + longitudeDelta / 2]
                                      }
                              };
    } else {
        requestDictionary = @{@"action" : @"query-radius",
                              @"request" : @{
                                      @"lat" : [NSNumber numberWithDouble:latitude],
                                      @"lng" : [NSNumber numberWithDouble:longitude],
                                      @"radiusInMeter" : [NSNumber numberWithDouble:AWSRadiusInMeter]
                                      }
                              };
    }

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

- (void)openTableView {
    AWSGeoListViewController *listViewController = [[AWSGeoListViewController alloc] initWithStyle:UITableViewStylePlain];
    listViewController.annotations = self.mapView.annotations;
    listViewController.center = [[CLLocation alloc] initWithLatitude:self.mapView.region.center.latitude
                                                           longitude:self.mapView.region.center.longitude];

    [self.navigationController pushViewController:listViewController animated:YES];
}

#pragma mark - MKMapViewDelegate methods

- (void)mapView:(MKMapView *)mapView regionDidChangeAnimated:(BOOL)animated {
    [self queryArea];
}

- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id <MKAnnotation>)annotation {
    MKAnnotationView *annotationView = [mapView dequeueReusableAnnotationViewWithIdentifier:@"Location"];
    if(!annotationView) {
        annotationView = [[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"Location"];

        annotationView.rightCalloutAccessoryView = [UIButton buttonWithType:UIButtonTypeDetailDisclosure];
        annotationView.enabled = YES;
        annotationView.canShowCallout = YES;
    }
    else {
        annotationView.annotation = annotation;
    }

    return annotationView;
}

- (void)mapView:(MKMapView *)mapView annotationView:(MKAnnotationView *)view calloutAccessoryControlTapped:(UIControl *)control {
    AWSPointAnnotation *annotation = (AWSPointAnnotation *) view.annotation;

    AWSDetailViewController *detailViewController = [[AWSDetailViewController alloc] initWithNibName:@"AWSDetailView" bundle:nil];
    detailViewController.rangeKey = annotation.rangeKey;
    detailViewController.coordinate = annotation.coordinate;
    detailViewController.title = annotation.title;

    [self.navigationController pushViewController:detailViewController animated:YES];
}

#pragma mark - NSURLConnection delegate methods

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
    [self.data appendData:data];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection {
    NSDictionary *resultDictionary = [NSJSONSerialization JSONObjectWithData:self.data
                                                                     options:kNilOptions
                                                                       error:nil];
    NSLog(@"Response:\n%@", resultDictionary);

    NSString *action = [resultDictionary objectForKey:@"action"];
    if([action isEqualToString:@"query"]) {
        [self.mapView removeAnnotations:self.mapView.annotations];

        for (NSDictionary *jsonDic in [resultDictionary objectForKey:@"result"]) {
            AWSPointAnnotation *annotation = [AWSPointAnnotation new];
            annotation.coordinate = CLLocationCoordinate2DMake([[jsonDic objectForKey:@"latitude"] doubleValue],
                                                               [[jsonDic objectForKey:@"longitude"] doubleValue]);
            annotation.title = [jsonDic objectForKey:@"schoolName"];
            annotation.rangeKey = [jsonDic objectForKey:@"rangeKey"];
            [self.mapView addAnnotation:annotation];
        }
    }
    else if([action isEqualToString:@"put-point"]) {
        [self queryArea];
    }
}

#pragma mark - UIActionSheetDelegate methods

- (void)showActionSheet {
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:nil
                                                             delegate:self
                                                    cancelButtonTitle:@"Cancel"
                                               destructiveButtonTitle:nil
                                                    otherButtonTitles:@"Show Table View", self.radiusQueryEnabled ? @"Switch to Box Query" : @"Switch to Radius Query", @"Add Center Point", nil];
    [actionSheet showInView:self.view];
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if(buttonIndex == 0) {
        [self openTableView];
    } else if(buttonIndex == 1) {
        self.radiusQueryEnabled = !self.radiusQueryEnabled;
        [self queryArea];
    } else if(buttonIndex == 2) {
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"School Name"
                                                            message:@"Please specify the school name."
                                                           delegate:self
                                                  cancelButtonTitle:@"Cancel"
                                                  otherButtonTitles:@"Add", nil];
        alertView.alertViewStyle = UIAlertViewStylePlainTextInput;
        [alertView show];
    }
}

#pragma mark - UIAlertViewDelegate methods

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if(buttonIndex == 1) {
        UITextField *textField = [alertView textFieldAtIndex:0];
        NSDictionary *requestDictionary = @{@"action" : @"put-point",
                                            @"request" : @{
                                                    @"lat" : [NSNumber numberWithDouble:self.mapView.centerCoordinate.latitude],
                                                    @"lng" : [NSNumber numberWithDouble:self.mapView.centerCoordinate.longitude],
                                                    @"schoolName" : textField.text
                                                    }
                                            };
        [self sendRequest:requestDictionary];
    }
}

@end
