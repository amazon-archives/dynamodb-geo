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

package com.amazonaws.geo.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.amazonaws.geo.model.GeoObject;
import com.amazonaws.geo.model.GeoPoint;

public class GeoJsonMapper {
	private static ObjectMapper mapper = new ObjectMapper();

	public static GeoPoint geoPointFromString(String jsonString) {
		try {
			return mapper.readValue(jsonString, GeoPoint.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String stringFromGeoObject(GeoObject geoObject) {
		try {
			return mapper.writeValueAsString(geoObject);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
