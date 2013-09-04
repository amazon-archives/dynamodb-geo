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

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;

/**
 * Delete point request. The request must specify a geo point and a range key value. You can modify DeleteItemRequest to
 * customize the underlining Amazon DynamoDB delete item request, but the table name, hash key, geohash, and geoJson
 * attribute will be overwritten by GeoDataManagerConfiguration.
 * 
 * */
public class DeletePointRequest extends GeoDataRequest {
	private DeleteItemRequest deleteItemRequest;
	private GeoPoint geoPoint;
	private AttributeValue rangeKeyValue;

	public DeletePointRequest(GeoPoint geoPoint, AttributeValue rangeKeyValue) {
		deleteItemRequest = new DeleteItemRequest();
		deleteItemRequest.setKey(new HashMap<String, AttributeValue>());

		this.geoPoint = geoPoint;
		this.rangeKeyValue = rangeKeyValue;
	}

	public DeleteItemRequest getDeleteItemRequest() {
		return deleteItemRequest;
	}

	public GeoPoint getGeoPoint() {
		return geoPoint;
	}

	public AttributeValue getRangeKeyValue() {
		return rangeKeyValue;
	}
}
