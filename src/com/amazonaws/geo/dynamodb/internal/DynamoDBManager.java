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

package com.amazonaws.geo.dynamodb.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.geo.GeoDataManagerConfiguration;
import com.amazonaws.geo.model.BatchWritePointResult;
import com.amazonaws.geo.model.DeletePointRequest;
import com.amazonaws.geo.model.DeletePointResult;
import com.amazonaws.geo.model.GeohashRange;
import com.amazonaws.geo.model.GetPointRequest;
import com.amazonaws.geo.model.GetPointResult;
import com.amazonaws.geo.model.PutPointRequest;
import com.amazonaws.geo.model.PutPointResult;
import com.amazonaws.geo.model.UpdatePointRequest;
import com.amazonaws.geo.model.UpdatePointResult;
import com.amazonaws.geo.s2.internal.S2Manager;
import com.amazonaws.geo.util.GeoJsonMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.PutRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;

public class DynamoDBManager {
	private GeoDataManagerConfiguration config;

	public DynamoDBManager(GeoDataManagerConfiguration config) {
		this.config = config;
	}

	/**
	 * Query Amazon DynamoDB
	 * 
	 * @param hashKey
	 *            Hash key for the query request.
	 * 
	 * @param range
	 *            The range of geohashs to query.
	 * 
	 * @return The query result.
	 */
	public List<QueryResult> queryGeohash(QueryRequest queryRequest, long hashKey, GeohashRange range) {
		List<QueryResult> queryResults = new ArrayList<QueryResult>();
		Map<String, AttributeValue> lastEvaluatedKey = null;

		do {
			Map<String, Condition> keyConditions = new HashMap<String, Condition>();

			Condition hashKeyCondition = new Condition().withComparisonOperator(ComparisonOperator.EQ)
					.withAttributeValueList(new AttributeValue().withN(String.valueOf(hashKey)));
			keyConditions.put(config.getHashKeyAttributeName(), hashKeyCondition);

			AttributeValue minRange = new AttributeValue().withN(Long.toString(range.getRangeMin()));
			AttributeValue maxRange = new AttributeValue().withN(Long.toString(range.getRangeMax()));

			Condition geohashCondition = new Condition().withComparisonOperator(ComparisonOperator.BETWEEN)
					.withAttributeValueList(minRange, maxRange);
			keyConditions.put(config.getGeohashAttributeName(), geohashCondition);

			queryRequest.withTableName(config.getTableName()).withKeyConditions(keyConditions)
					.withIndexName(config.getGeohashIndexName()).withConsistentRead(true)
					.withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL).withExclusiveStartKey(lastEvaluatedKey);

			QueryResult queryResult = config.getDynamoDBClient().query(queryRequest);
			queryResults.add(queryResult);

			lastEvaluatedKey = queryResult.getLastEvaluatedKey();

		} while (lastEvaluatedKey != null);

		return queryResults;
	}

	public GetPointResult getPoint(GetPointRequest getPointRequest) {
		long geohash = S2Manager.generateGeohash(getPointRequest.getGeoPoint());
		long hashKey = S2Manager.generateHashKey(geohash, config.getHashKeyLength());

		GetItemRequest getItemRequest = getPointRequest.getGetItemRequest();
		getItemRequest.setTableName(config.getTableName());

		AttributeValue hashKeyValue = new AttributeValue().withN(String.valueOf(hashKey));
		getItemRequest.getKey().put(config.getHashKeyAttributeName(), hashKeyValue);
		getItemRequest.getKey().put(config.getRangeKeyAttributeName(), getPointRequest.getRangeKeyValue());

		GetItemResult getItemResult = config.getDynamoDBClient().getItem(getItemRequest);
		GetPointResult getPointResult = new GetPointResult(getItemResult);

		return getPointResult;
	}

	public PutPointResult putPoint(PutPointRequest putPointRequest) {
		long geohash = S2Manager.generateGeohash(putPointRequest.getGeoPoint());
		long hashKey = S2Manager.generateHashKey(geohash, config.getHashKeyLength());
		String geoJson = GeoJsonMapper.stringFromGeoObject(putPointRequest.getGeoPoint());

		PutItemRequest putItemRequest = putPointRequest.getPutItemRequest();
		putItemRequest.setTableName(config.getTableName());

		AttributeValue hashKeyValue = new AttributeValue().withN(String.valueOf(hashKey));
		putItemRequest.getItem().put(config.getHashKeyAttributeName(), hashKeyValue);
		putItemRequest.getItem().put(config.getRangeKeyAttributeName(), putPointRequest.getRangeKeyValue());
		AttributeValue geohashValue = new AttributeValue().withN(Long.toString(geohash));
		putItemRequest.getItem().put(config.getGeohashAttributeName(), geohashValue);
		AttributeValue geoJsonValue = new AttributeValue().withS(geoJson);
		putItemRequest.getItem().put(config.getGeoJsonAttributeName(), geoJsonValue);

		PutItemResult putItemResult = config.getDynamoDBClient().putItem(putItemRequest);
		PutPointResult putPointResult = new PutPointResult(putItemResult);

		return putPointResult;
	}
	
	public BatchWritePointResult batchWritePoints(List<PutPointRequest> putPointRequests) {
		BatchWriteItemRequest batchItemRequest = new BatchWriteItemRequest();
		List<WriteRequest> writeRequests = new ArrayList<WriteRequest>();
		for (PutPointRequest putPointRequest : putPointRequests) {
			long geohash = S2Manager.generateGeohash(putPointRequest.getGeoPoint());
			long hashKey = S2Manager.generateHashKey(geohash, config.getHashKeyLength());
			String geoJson = GeoJsonMapper.stringFromGeoObject(putPointRequest.getGeoPoint());

			PutRequest putRequest = putPointRequest.getPutRequest();
			AttributeValue hashKeyValue = new AttributeValue().withN(String.valueOf(hashKey));
			putRequest.getItem().put(config.getHashKeyAttributeName(), hashKeyValue);
			putRequest.getItem().put(config.getRangeKeyAttributeName(), putPointRequest.getRangeKeyValue());
			AttributeValue geohashValue = new AttributeValue().withN(Long.toString(geohash));
			putRequest.getItem().put(config.getGeohashAttributeName(), geohashValue);
			AttributeValue geoJsonValue = new AttributeValue().withS(geoJson);
			putRequest.getItem().put(config.getGeoJsonAttributeName(), geoJsonValue);			
			
			WriteRequest writeRequest = new WriteRequest(putRequest);
			writeRequests.add(writeRequest);
		}
		Map<String, List<WriteRequest>> requestItems = new HashMap<String, List<WriteRequest>>();
		requestItems.put(config.getTableName(), writeRequests);
		batchItemRequest.setRequestItems(requestItems);
		BatchWriteItemResult batchWriteItemResult = config.getDynamoDBClient().batchWriteItem(batchItemRequest);
		BatchWritePointResult batchWritePointResult = new BatchWritePointResult(batchWriteItemResult);
		return batchWritePointResult;
	}

	public UpdatePointResult updatePoint(UpdatePointRequest updatePointRequest) {
		long geohash = S2Manager.generateGeohash(updatePointRequest.getGeoPoint());
		long hashKey = S2Manager.generateHashKey(geohash, config.getHashKeyLength());

		UpdateItemRequest updateItemRequest = updatePointRequest.getUpdateItemRequest();
		updateItemRequest.setTableName(config.getTableName());

		AttributeValue hashKeyValue = new AttributeValue().withN(String.valueOf(hashKey));
		updateItemRequest.getKey().put(config.getHashKeyAttributeName(), hashKeyValue);
		updateItemRequest.getKey().put(config.getRangeKeyAttributeName(), updatePointRequest.getRangeKeyValue());

		// Geohash and geoJson cannot be updated.
		updateItemRequest.getAttributeUpdates().remove(config.getGeohashAttributeName());
		updateItemRequest.getAttributeUpdates().remove(config.getGeoJsonAttributeName());

		UpdateItemResult updateItemResult = config.getDynamoDBClient().updateItem(updateItemRequest);
		UpdatePointResult updatePointResult = new UpdatePointResult(updateItemResult);

		return updatePointResult;
	}

	public DeletePointResult deletePoint(DeletePointRequest deletePointRequest) {
		long geohash = S2Manager.generateGeohash(deletePointRequest.getGeoPoint());
		long hashKey = S2Manager.generateHashKey(geohash, config.getHashKeyLength());

		DeleteItemRequest deleteItemRequest = deletePointRequest.getDeleteItemRequest();

		deleteItemRequest.setTableName(config.getTableName());

		AttributeValue hashKeyValue = new AttributeValue().withN(String.valueOf(hashKey));
		deleteItemRequest.getKey().put(config.getHashKeyAttributeName(), hashKeyValue);
		deleteItemRequest.getKey().put(config.getRangeKeyAttributeName(), deletePointRequest.getRangeKeyValue());

		DeleteItemResult deleteItemResult = config.getDynamoDBClient().deleteItem(deleteItemRequest);
		DeletePointResult deletePointResult = new DeletePointResult(deleteItemResult);

		return deletePointResult;
	}
}
