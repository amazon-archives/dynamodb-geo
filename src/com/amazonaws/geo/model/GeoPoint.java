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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GeoPoint extends GeoObject {

	protected double[] coordinates;

	public GeoPoint() {
		type = "Point";
	}

	public GeoPoint(double latitude, double longitude) {
		this();
		setCoordinates(new double[] { latitude, longitude });
	}

	@JsonIgnore
	public double getLatitude() {
		return coordinates[0];
	}

	@JsonIgnore
	public double getLongitude() {
		return coordinates[1];
	}

	public double[] getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(double[] coordinates) {
		this.coordinates = coordinates;
	}
}
