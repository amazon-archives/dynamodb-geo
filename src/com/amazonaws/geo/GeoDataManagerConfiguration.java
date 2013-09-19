/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * 
 * http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.geo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class GeoDataManagerConfiguration {

	// Public constants
	public static final long MERGE_THRESHOLD = 2;

	// Default values
	private static final String DEFAULT_HASHKEY_ATTRIBUTE_NAME = "hashKey";
	private static final String DEFAULT_RANGEKEY_ATTRIBUTE_NAME = "rangeKey";
	private static final String DEFAULT_GEOHASH_ATTRIBUTE_NAME = "geohash";
	private static final String DEFAULT_GEOJSON_ATTRIBUTE_NAME = "geoJson";

	private static final String DEFAULT_GEOHASH_INDEX_ATTRIBUTE_NAME = "geohash-index";

	private static final int DEFAULT_HASHKEY_LENGTH = 6;

	private static final int DEFAULT_THREAD_POOL_SIZE = 10;

	// Configuration properties
	private String tableName;

	private String hashKeyAttributeName;
	private String rangeKeyAttributeName;
	private String geohashAttributeName;
	private String geoJsonAttributeName;

	private String geohashIndexName;

	private int hashKeyLength;

	private AmazonDynamoDBClient dynamoDBClient;

	private ExecutorService executorService;

	public GeoDataManagerConfiguration(AmazonDynamoDBClient dynamoDBClient, String tableName) {
		hashKeyAttributeName = DEFAULT_HASHKEY_ATTRIBUTE_NAME;
		rangeKeyAttributeName = DEFAULT_RANGEKEY_ATTRIBUTE_NAME;
		geohashAttributeName = DEFAULT_GEOHASH_ATTRIBUTE_NAME;
		geoJsonAttributeName = DEFAULT_GEOJSON_ATTRIBUTE_NAME;

		geohashIndexName = DEFAULT_GEOHASH_INDEX_ATTRIBUTE_NAME;

		hashKeyLength = DEFAULT_HASHKEY_LENGTH;

		this.dynamoDBClient = dynamoDBClient;
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getHashKeyAttributeName() {
		return hashKeyAttributeName;
	}

	public void setHashKeyAttributeName(String hashKeyAttributeName) {
		this.hashKeyAttributeName = hashKeyAttributeName;
	}

	public GeoDataManagerConfiguration withHashKeyAttributeName(String hashKeyAttributeName) {
		setHashKeyAttributeName(hashKeyAttributeName);
		return this;
	}

	public String getRangeKeyAttributeName() {
		return rangeKeyAttributeName;
	}

	public void setRangeKeyAttributeName(String rangeKeyAttributeName) {
		this.rangeKeyAttributeName = rangeKeyAttributeName;
	}

	public GeoDataManagerConfiguration withRangeKeyAttributeName(String rangeKeyAttributeName) {
		setRangeKeyAttributeName(rangeKeyAttributeName);
		return this;
	}

	public String getGeohashAttributeName() {
		return geohashAttributeName;
	}

	public void setGeohashAttributeName(String geohashAttributeName) {
		this.geohashAttributeName = geohashAttributeName;
	}

	public GeoDataManagerConfiguration withGeohashAttributeName(String geohashAttributeName) {
		setGeohashAttributeName(geohashAttributeName);
		return this;
	}

	public String getGeoJsonAttributeName() {
		return geoJsonAttributeName;
	}

	public void setGeoJsonAttributeName(String geoJsonAttributeName) {
		this.geoJsonAttributeName = geoJsonAttributeName;
	}

	public GeoDataManagerConfiguration withGeoJsonAttributeName(String geoJsonAttributeName) {
		setGeoJsonAttributeName(geoJsonAttributeName);
		return this;
	}

	public String getGeohashIndexName() {
		return geohashIndexName;
	}

	public void setGeohashIndexName(String geohashIndexName) {
		this.geohashIndexName = geohashIndexName;
	}

	public GeoDataManagerConfiguration withGeohashIndexName(String geohashIndexName) {
		setGeohashIndexName(geohashIndexName);
		return this;
	}

	public int getHashKeyLength() {
		return hashKeyLength;
	}

	public void setHashKeyLength(int hashKeyLength) {
		this.hashKeyLength = hashKeyLength;
	}

	public GeoDataManagerConfiguration withHashKeyLength(int hashKeyLength) {
		setHashKeyLength(hashKeyLength);
		return this;
	}

	public AmazonDynamoDBClient getDynamoDBClient() {
		return dynamoDBClient;
	}

	public void setDynamoDBClient(AmazonDynamoDBClient dynamoDBClient) {
		this.dynamoDBClient = dynamoDBClient;
	}

	public ExecutorService getExecutorService() {
		synchronized (this) {
			if (executorService == null) {
				executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
			}
		}

		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		synchronized (this) {
			this.executorService = executorService;
		}
	}
}
