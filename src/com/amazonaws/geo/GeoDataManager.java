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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.amazonaws.AmazonClientException;
import com.amazonaws.geo.dynamodb.internal.DynamoDBManager;
import com.amazonaws.geo.dynamodb.internal.DynamoDBUtil;
import com.amazonaws.geo.model.BatchWritePointResult;
import com.amazonaws.geo.model.DeletePointRequest;
import com.amazonaws.geo.model.DeletePointResult;
import com.amazonaws.geo.model.GeoPoint;
import com.amazonaws.geo.model.GeoQueryRequest;
import com.amazonaws.geo.model.GeoQueryResult;
import com.amazonaws.geo.model.GeohashRange;
import com.amazonaws.geo.model.GetPointRequest;
import com.amazonaws.geo.model.GetPointResult;
import com.amazonaws.geo.model.PutPointRequest;
import com.amazonaws.geo.model.PutPointResult;
import com.amazonaws.geo.model.QueryRadiusRequest;
import com.amazonaws.geo.model.QueryRadiusResult;
import com.amazonaws.geo.model.QueryRectangleRequest;
import com.amazonaws.geo.model.QueryRectangleResult;
import com.amazonaws.geo.model.UpdatePointRequest;
import com.amazonaws.geo.model.UpdatePointResult;
import com.amazonaws.geo.s2.internal.S2Manager;
import com.amazonaws.geo.s2.internal.S2Util;
import com.amazonaws.geo.util.GeoJsonMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2LatLngRect;

/**
 * <p>
 * Manager to hangle geo spatial data in Amazon DynamoDB tables. All service calls made using this client are blocking,
 * and will not return until the service call completes.
 * </p>
 * <p>
 * This class is designed to be thread safe; however, once constructed GeoDataManagerConfiguration should not be
 * modified. Modifying GeoDataManagerConfiguration may cause unspecified behaviors.
 * </p>
 * */
public class GeoDataManager {
	private GeoDataManagerConfiguration config;
	private DynamoDBManager dynamoDBManager;

	/**
	 * <p>
	 * Construct and configure GeoDataManager using GeoDataManagerConfiguration.
	 * </p>
	 * <b>Sample usage:</b>
	 * 
	 * <pre>
	 * AmazonDynamoDBClient ddb = new AmazonDynamoDBClient(new ClasspathPropertiesFileCredentialsProvider());
	 * Region usWest2 = Region.getRegion(Regions.US_WEST_2);
	 * ddb.setRegion(usWest2);
	 * 
	 * ClientConfiguration clientConfiguration = new ClientConfiguration().withMaxErrorRetry(5);
	 * ddb.setConfiguration(clientConfiguration);
	 * 
	 * GeoDataManagerConfiguration config = new GeoDataManagerConfiguration(ddb, &quot;geo-table&quot;);
	 * GeoDataManager geoDataManager = new GeoDataManager(config);
	 * </pre>
	 * 
	 * @param config
	 *            Container for the configuration parameters for GeoDataManager.
	 */
	public GeoDataManager(GeoDataManagerConfiguration config) {
		this.config = config;
		dynamoDBManager = new DynamoDBManager(this.config);
	}

	/**
	 * <p>
	 * Return GeoDataManagerConfiguration. The returned GeoDataManagerConfiguration should not be modified.
	 * </p>
	 * 
	 * @return
	 *         GeoDataManagerConfiguration that is used to configure this GeoDataManager.
	 */
	public GeoDataManagerConfiguration getGeoDataManagerConfiguration() {
		return config;
	}

	/**
	 * <p>
	 * Put a point into the Amazon DynamoDB table. Once put, you cannot update attributes specified in
	 * GeoDataManagerConfiguration: hash key, range key, geohash and geoJson. If you want to update these columns, you
	 * need to insert a new record and delete the old record.
	 * </p>
	 * <b>Sample usage:</b>
	 * 
	 * <pre>
	 * GeoPoint geoPoint = new GeoPoint(47.5, -122.3);
	 * AttributeValue rangeKeyValue = new AttributeValue().withS(&quot;a6feb446-c7f2-4b48-9b3a-0f87744a5047&quot;);
	 * AttributeValue titleValue = new AttributeValue().withS(&quot;Original title&quot;);
	 * 
	 * PutPointRequest putPointRequest = new PutPointRequest(geoPoint, rangeKeyValue);
	 * putPointRequest.getPutItemRequest().getItem().put(&quot;title&quot;, titleValue);
	 * 
	 * PutPointResult putPointResult = geoDataManager.putPoint(putPointRequest);
	 * </pre>
	 * 
	 * @param putPointRequest
	 *            Container for the necessary parameters to execute put point request.
	 * 
	 * @return Result of put point request.
	 */
	public PutPointResult putPoint(PutPointRequest putPointRequest) {
		return dynamoDBManager.putPoint(putPointRequest);
	}
	
	/**
	 * <p>
	 * Put a list of points into the Amazon DynamoDB table. Once put, you cannot update attributes specified in
	 * GeoDataManagerConfiguration: hash key, range key, geohash and geoJson. If you want to update these columns, you
	 * need to insert a new record and delete the old record.
	 * </p>
	 * <b>Sample usage:</b>
	 * 
	 * <pre>
	 * GeoPoint geoPoint = new GeoPoint(47.5, -122.3);
	 * AttributeValue rangeKeyValue = new AttributeValue().withS(&quot;a6feb446-c7f2-4b48-9b3a-0f87744a5047&quot;);
	 * AttributeValue titleValue = new AttributeValue().withS(&quot;Original title&quot;);
	 * 
	 * PutPointRequest putPointRequest = new PutPointRequest(geoPoint, rangeKeyValue);
	 * putPointRequest.getPutItemRequest().getItem().put(&quot;title&quot;, titleValue);
	 * List<PutPointRequest> putPointRequests = new ArrayList<PutPointRequest>();
	 * putPointRequests.add(putPointRequest);
	 * BatchWritePointResult batchWritePointResult = geoDataManager.batchWritePoints(putPointRequests);
	 * </pre>
	 * 
	 * @param putPointRequests
	 *            Container for the necessary parameters to execute put point request.
	 * 
	 * @return Result of batch put point request.
	 */	
	public BatchWritePointResult batchWritePoints(List<PutPointRequest> putPointRequests) {
		return dynamoDBManager.batchWritePoints(putPointRequests);
	}

	/**
	 * <p>
	 * Get a point from the Amazon DynamoDB table.
	 * </p>
	 * <b>Sample usage:</b>
	 * 
	 * <pre>
	 * GeoPoint geoPoint = new GeoPoint(47.5, -122.3);
	 * AttributeValue rangeKeyValue = new AttributeValue().withS(&quot;a6feb446-c7f2-4b48-9b3a-0f87744a5047&quot;);
	 * 
	 * GetPointRequest getPointRequest = new GetPointRequest(geoPoint, rangeKeyValue);
	 * GetPointResult getPointResult = geoIndexManager.getPoint(getPointRequest);
	 * 
	 * System.out.println(&quot;item: &quot; + getPointResult.getGetItemResult().getItem());
	 * </pre>
	 * 
	 * @param getPointRequest
	 *            Container for the necessary parameters to execute get point request.
	 * 
	 * @return Result of get point request.
	 * */
	public GetPointResult getPoint(GetPointRequest getPointRequest) {
		return dynamoDBManager.getPoint(getPointRequest);
	}

	/**
	 * <p>
	 * Query a rectangular area constructed by two points and return all points within the area. Two points need to
	 * construct a rectangle from minimum and maximum latitudes and longitudes. If minPoint.getLongitude() >
	 * maxPoint.getLongitude(), the rectangle spans the 180 degree longitude line.
	 * </p>
	 * <b>Sample usage:</b>
	 * 
	 * <pre>
	 * GeoPoint minPoint = new GeoPoint(45.5, -124.3);
	 * GeoPoint maxPoint = new GeoPoint(49.5, -120.3);
	 * 
	 * QueryRectangleRequest queryRectangleRequest = new QueryRectangleRequest(minPoint, maxPoint);
	 * QueryRectangleResult queryRectangleResult = geoIndexManager.queryRectangle(queryRectangleRequest);
	 * 
	 * for (Map&lt;String, AttributeValue&gt; item : queryRectangleResult.getItem()) {
	 * 	System.out.println(&quot;item: &quot; + item);
	 * }
	 * </pre>
	 * 
	 * @param queryRectangleRequest
	 *            Container for the necessary parameters to execute rectangle query request.
	 * 
	 * @return Result of rectangle query request.
	 */
	public QueryRectangleResult queryRectangle(QueryRectangleRequest queryRectangleRequest) {
		S2LatLngRect latLngRect = S2Util.getBoundingLatLngRect(queryRectangleRequest);

		S2CellUnion cellUnion = S2Manager.findCellIds(latLngRect);

		List<GeohashRange> ranges = mergeCells(cellUnion);
		cellUnion = null;

		return new QueryRectangleResult(dispatchQueries(ranges, queryRectangleRequest));
	}

	/**
	 * <p>
	 * Query a circular area constructed by a center point and its radius.
	 * </p>
	 * <b>Sample usage:</b>
	 * 
	 * <pre>
	 * GeoPoint centerPoint = new GeoPoint(47.5, -122.3);
	 * 
	 * QueryRadiusRequest queryRadiusRequest = new QueryRadiusRequest(centerPoint, 100);
	 * QueryRadiusResult queryRadiusResult = geoIndexManager.queryRadius(queryRadiusRequest);
	 * 
	 * for (Map&lt;String, AttributeValue&gt; item : queryRadiusResult.getItem()) {
	 * 	System.out.println(&quot;item: &quot; + item);
	 * }
	 * </pre>
	 * 
	 * @param queryRadiusRequest
	 *            Container for the necessary parameters to execute radius query request.
	 * 
	 * @return Result of radius query request.
	 * */
	public QueryRadiusResult queryRadius(QueryRadiusRequest queryRadiusRequest) {
		S2LatLngRect latLngRect = S2Util.getBoundingLatLngRect(queryRadiusRequest);

		S2CellUnion cellUnion = S2Manager.findCellIds(latLngRect);

		List<GeohashRange> ranges = mergeCells(cellUnion);
		cellUnion = null;

		return new QueryRadiusResult(dispatchQueries(ranges, queryRadiusRequest));
	}

	/**
	 * <p>
	 * Update a point data in Amazon DynamoDB table. You cannot update attributes specified in
	 * GeoDataManagerConfiguration: hash key, range key, geohash and geoJson. If you want to update these columns, you
	 * need to insert a new record and delete the old record.
	 * </p>
	 * <b>Sample usage:</b>
	 * 
	 * <pre>
	 * GeoPoint geoPoint = new GeoPoint(47.5, -122.3);
	 * 
	 * String rangeKey = &quot;a6feb446-c7f2-4b48-9b3a-0f87744a5047&quot;;
	 * AttributeValue rangeKeyValue = new AttributeValue().withS(rangeKey);
	 * 
	 * UpdatePointRequest updatePointRequest = new UpdatePointRequest(geoPoint, rangeKeyValue);
	 * 
	 * AttributeValue titleValue = new AttributeValue().withS(&quot;Updated title.&quot;);
	 * AttributeValueUpdate titleValueUpdate = new AttributeValueUpdate().withAction(AttributeAction.PUT)
	 * 		.withValue(titleValue);
	 * updatePointRequest.getUpdateItemRequest().getAttributeUpdates().put(&quot;title&quot;, titleValueUpdate);
	 * 
	 * UpdatePointResult updatePointResult = geoIndexManager.updatePoint(updatePointRequest);
	 * </pre>
	 * 
	 * @param updatePointRequest
	 *            Container for the necessary parameters to execute update point request.
	 * 
	 * @return Result of update point request.
	 */
	public UpdatePointResult updatePoint(UpdatePointRequest updatePointRequest) {
		return dynamoDBManager.updatePoint(updatePointRequest);
	}

	/**
	 * <p>
	 * Delete a point from the Amazon DynamoDB table.
	 * </p>
	 * <b>Sample usage:</b>
	 * 
	 * <pre>
	 * GeoPoint geoPoint = new GeoPoint(47.5, -122.3);
	 * 
	 * String rangeKey = &quot;a6feb446-c7f2-4b48-9b3a-0f87744a5047&quot;;
	 * AttributeValue rangeKeyValue = new AttributeValue().withS(rangeKey);
	 * 
	 * DeletePointRequest deletePointRequest = new DeletePointRequest(geoPoint, rangeKeyValue);
	 * DeletePointResult deletePointResult = geoIndexManager.deletePoint(deletePointRequest);
	 * </pre>
	 * 
	 * @param deletePointRequest
	 *            Container for the necessary parameters to execute delete point request.
	 * 
	 * @return Result of delete point request.
	 */
	public DeletePointResult deletePoint(DeletePointRequest deletePointRequest) {
		return dynamoDBManager.deletePoint(deletePointRequest);
	}

	/**
	 * Merge continuous cells in cellUnion and return a list of merged GeohashRanges.
	 * 
	 * @param cellUnion
	 *            Container for multiple cells.
	 * 
	 * @return A list of merged GeohashRanges.
	 */
	private List<GeohashRange> mergeCells(S2CellUnion cellUnion) {

		List<GeohashRange> ranges = new ArrayList<GeohashRange>();
		for (S2CellId c : cellUnion.cellIds()) {
			GeohashRange range = new GeohashRange(c.rangeMin().id(), c.rangeMax().id());

			boolean wasMerged = false;
			for (GeohashRange r : ranges) {
				if (r.tryMerge(range)) {
					wasMerged = true;
					break;
				}
			}

			if (!wasMerged) {
				ranges.add(range);
			}
		}

		return ranges;
	}

	/**
	 * Query Amazon DynamoDB in parallel and filter the result.
	 * 
	 * @param ranges
	 *            A list of geohash ranges that will be used to query Amazon DynamoDB.
	 * 
	 * @param latLngRect
	 *            The rectangle area that will be used as a reference point for precise filtering.
	 * 
	 * @return Aggregated and filtered items returned from Amazon DynamoDB.
	 */
	private GeoQueryResult dispatchQueries(List<GeohashRange> ranges, GeoQueryRequest geoQueryRequest) {
		GeoQueryResult geoQueryResult = new GeoQueryResult();

		ExecutorService executorService = config.getExecutorService();
		List<Future<?>> futureList = new ArrayList<Future<?>>();

		for (GeohashRange outerRange : ranges) {
			for (GeohashRange range : outerRange.trySplit(config.getHashKeyLength())) {
				GeoQueryThread geoQueryThread = new GeoQueryThread(geoQueryRequest, geoQueryResult, range);
				futureList.add(executorService.submit(geoQueryThread));
			}
		}
		ranges = null;

		for (int i = 0; i < futureList.size(); i++) {
			try {
				futureList.get(i).get();
			} catch (Exception e) {
				for (int j = i + 1; j < futureList.size(); j++) {
					futureList.get(j).cancel(true);
				}
				throw new AmazonClientException("Querying Amazon DynamoDB failed.", e);
			}
		}
		futureList = null;

		return geoQueryResult;
	}

	/**
	 * Filter out any points outside of the queried area from the input list.
	 * 
	 * @param list
	 *            List of items return by Amazon DynamoDB. It may contains points outside of the actual area queried.
	 * 
	 * @param latLngRect
	 *            Queried area. Any points outside of this area need to be discarded.
	 * 
	 * @return List of items within the queried area.
	 */
	private List<Map<String, AttributeValue>> filter(List<Map<String, AttributeValue>> list,
			GeoQueryRequest geoQueryRequest) {

		List<Map<String, AttributeValue>> result = new ArrayList<Map<String, AttributeValue>>();

		S2LatLngRect latLngRect = null;
		S2LatLng centerLatLng = null;
		double radiusInMeter = 0;
		if (geoQueryRequest instanceof QueryRectangleRequest) {
			latLngRect = S2Util.getBoundingLatLngRect(geoQueryRequest);
		} else if (geoQueryRequest instanceof QueryRadiusRequest) {
			GeoPoint centerPoint = ((QueryRadiusRequest) geoQueryRequest).getCenterPoint();
			centerLatLng = S2LatLng.fromDegrees(centerPoint.getLatitude(), centerPoint.getLongitude());

			radiusInMeter = ((QueryRadiusRequest) geoQueryRequest).getRadiusInMeter();
		}

		for (Map<String, AttributeValue> item : list) {
			String geoJson = item.get(config.getGeoJsonAttributeName()).getS();
			GeoPoint geoPoint = GeoJsonMapper.geoPointFromString(geoJson);

			S2LatLng latLng = S2LatLng.fromDegrees(geoPoint.getLatitude(), geoPoint.getLongitude());
			if (latLngRect != null && latLngRect.contains(latLng)) {
				result.add(item);
			} else if (centerLatLng != null && radiusInMeter > 0
					&& centerLatLng.getEarthDistance(latLng) <= radiusInMeter) {
				result.add(item);
			}
		}

		return result;
	}

	/**
	 * Worker thread to query Amazon DynamoDB.
	 * */
	private class GeoQueryThread extends Thread {
		private GeoQueryRequest geoQueryRequest;
		private GeoQueryResult geoQueryResult;
		private GeohashRange range;

		public GeoQueryThread(GeoQueryRequest geoQueryRequest, GeoQueryResult geoQueryResult, GeohashRange range) {
			this.geoQueryRequest = geoQueryRequest;
			this.geoQueryResult = geoQueryResult;
			this.range = range;
		}

		public void run() {
			QueryRequest queryRequest = DynamoDBUtil.copyQueryRequest(geoQueryRequest.getQueryRequest());
			long hashKey = S2Manager.generateHashKey(range.getRangeMin(), config.getHashKeyLength());

			List<QueryResult> queryResults = dynamoDBManager.queryGeohash(queryRequest, hashKey, range);

			for (QueryResult queryResult : queryResults) {
				if (isInterrupted()) {
					return;
				}

				// getQueryResults() returns a synchronized list.
				geoQueryResult.getQueryResults().add(queryResult);

				List<Map<String, AttributeValue>> filteredQueryResult = filter(queryResult.getItems(), geoQueryRequest);

				// getItem() returns a synchronized list.
				geoQueryResult.getItem().addAll(filteredQueryResult);
			}
		}
	}
}
