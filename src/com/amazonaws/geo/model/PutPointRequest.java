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

package com.amazonaws.geo.model;

import java.util.HashMap;

import com.amazonaws.geo.GeoDataManagerConfiguration;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutRequest;

/**
 * Put point request. The request must specify a geo point and a range key value. You can modify PutItemRequest to
 * customize the underlining Amazon DynamoDB put item request, but the table name, hash key, geohash, and geoJson
 * attribute will be overwritten by GeoDataManagerConfiguration.
 * 
 * @see GeoDataManagerConfiguration
 * 
 * */
public class PutPointRequest extends GeoDataRequest {
	private PutItemRequest putItemRequest;
	private PutRequest putRequest;
	private GeoPoint geoPoint;
	private AttributeValue rangeKeyValue;

	public PutPointRequest(GeoPoint geoPoint, AttributeValue rangeKeyValue) {
		putItemRequest = new PutItemRequest();
		putItemRequest.setItem(new HashMap<String, AttributeValue>());
		putRequest = new PutRequest();
		putRequest.setItem(new HashMap<String, AttributeValue>());
		
		this.geoPoint = geoPoint;
		this.rangeKeyValue = rangeKeyValue;
	}

	public PutItemRequest getPutItemRequest() {
		return putItemRequest;
	}

	public GeoPoint getGeoPoint() {
		return geoPoint;
	}

	public AttributeValue getRangeKeyValue() {
		return rangeKeyValue;
	}
	
	public PutRequest getPutRequest() {
		return putRequest;
	}
}
