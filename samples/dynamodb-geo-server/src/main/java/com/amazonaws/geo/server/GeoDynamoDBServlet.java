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

package com.amazonaws.geo.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.geo.GeoDataManager;
import com.amazonaws.geo.GeoDataManagerConfiguration;
import com.amazonaws.geo.model.DeletePointRequest;
import com.amazonaws.geo.model.DeletePointResult;
import com.amazonaws.geo.model.GeoPoint;
import com.amazonaws.geo.model.GeoQueryResult;
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
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

/**
 * Servlet implementation class GeoDynamoDBServlet
 */
public class GeoDynamoDBServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private GeoDataManagerConfiguration config;
	private GeoDataManager geoDataManager;

	private ObjectMapper mapper;
	private JsonFactory factory;

	public void init() throws ServletException {
		setupGeoDataManager();

		mapper = new ObjectMapper();
		factory = mapper.getJsonFactory();
	}

	private void setupGeoDataManager() {
		String accessKey = System.getProperty("AWS_ACCESS_KEY_ID");
		String secretKey = System.getProperty("AWS_SECRET_KEY");
		String tableName = System.getProperty("PARAM1");
		String regionName = System.getProperty("PARAM2");

		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		AmazonDynamoDBClient ddb = new AmazonDynamoDBClient(credentials);
		Region region = Region.getRegion(Regions.fromName(regionName));
		ddb.setRegion(region);

		config = new GeoDataManagerConfiguration(ddb, tableName);
		geoDataManager = new GeoDataManager(config);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		try {
			StringBuffer buffer = new StringBuffer();
			String line = null;
			BufferedReader reader = request.getReader();

			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

			JSONObject jsonObject = new JSONObject(buffer.toString());
			PrintWriter out = response.getWriter();

			String action = jsonObject.getString("action");
			log("action: " + action);
			JSONObject requestObject = jsonObject.getJSONObject("request");
			log("requestObject: " + requestObject);

			if (action.equalsIgnoreCase("put-point")) {
				putPoint(requestObject, out);
			} else if (action.equalsIgnoreCase("get-point")) {
				getPoint(requestObject, out);
			} else if (action.equalsIgnoreCase("update-point")) {
				updatePoint(requestObject, out);
			} else if (action.equalsIgnoreCase("query-rectangle")) {
				queryRectangle(requestObject, out);
			} else if (action.equalsIgnoreCase("query-radius")) {
				queryRadius(requestObject, out);
			} else if (action.equalsIgnoreCase("delete-point")) {
				deletePoint(requestObject, out);
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			log(sw.toString());
		}
	}

	private void putPoint(JSONObject requestObject, PrintWriter out) throws IOException, JSONException {
		GeoPoint geoPoint = new GeoPoint(requestObject.getDouble("lat"), requestObject.getDouble("lng"));
		AttributeValue rangeKeyAttributeValue = new AttributeValue().withS(UUID.randomUUID().toString());
		AttributeValue schoolNameKeyAttributeValue = new AttributeValue().withS(requestObject.getString("schoolName"));

		PutPointRequest putPointRequest = new PutPointRequest(geoPoint, rangeKeyAttributeValue);
		putPointRequest.getPutItemRequest().addItemEntry("schoolName", schoolNameKeyAttributeValue);

		PutPointResult putPointResult = geoDataManager.putPoint(putPointRequest);

		printPutPointResult(putPointResult, out);
	}

	private void printPutPointResult(PutPointResult putPointResult, PrintWriter out) throws JsonParseException,
			IOException {

		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("action", "put-point");

		out.println(mapper.writeValueAsString(jsonMap));
		out.flush();
	}

	private void getPoint(JSONObject requestObject, PrintWriter out) throws IOException, JSONException {
		GeoPoint geoPoint = new GeoPoint(requestObject.getDouble("lat"), requestObject.getDouble("lng"));
		AttributeValue rangeKeyAttributeValue = new AttributeValue().withS(requestObject.getString("rangeKey"));

		GetPointRequest getPointRequest = new GetPointRequest(geoPoint, rangeKeyAttributeValue);
		GetPointResult getPointResult = geoDataManager.getPoint(getPointRequest);

		printGetPointRequest(getPointResult, out);
	}

	private void printGetPointRequest(GetPointResult getPointResult, PrintWriter out) throws JsonParseException,
			IOException {
		Map<String, AttributeValue> item = getPointResult.getGetItemResult().getItem();
		String geoJsonString = item.get(config.getGeoJsonAttributeName()).getS();
		JsonParser jsonParser = factory.createJsonParser(geoJsonString);
		JsonNode jsonNode = mapper.readTree(jsonParser);

		double latitude = jsonNode.get("coordinates").get(0).getDoubleValue();
		double longitude = jsonNode.get("coordinates").get(1).getDoubleValue();
		String hashKey = item.get(config.getHashKeyAttributeName()).getN();
		String rangeKey = item.get(config.getRangeKeyAttributeName()).getS();
		String geohash = item.get(config.getGeohashAttributeName()).getN();
		String schoolName = item.get("schoolName").getS();
		String memo = "";
		if (item.containsKey("memo")) {
			memo = item.get("memo").getS();
		}

		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("latitude", Double.toString(latitude));
		resultMap.put("longitude", Double.toString(longitude));
		resultMap.put("hashKey", hashKey);
		resultMap.put("rangeKey", rangeKey);
		resultMap.put("geohash", geohash);
		resultMap.put("schoolName", schoolName);
		resultMap.put("memo", memo);

		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("action", "get-point");
		jsonMap.put("result", resultMap);

		out.println(mapper.writeValueAsString(jsonMap));
		out.flush();
	}

	private void updatePoint(JSONObject requestObject, PrintWriter out) throws IOException, JSONException {
		GeoPoint geoPoint = new GeoPoint(requestObject.getDouble("lat"), requestObject.getDouble("lng"));
		AttributeValue rangeKeyAttributeValue = new AttributeValue().withS(requestObject.getString("rangeKey"));

		String schoolName = requestObject.getString("schoolName");
		AttributeValueUpdate schoolNameValueUpdate = null;

		String memo = requestObject.getString("memo");
		AttributeValueUpdate memoValueUpdate = null;

		if (schoolName == null || schoolName.equalsIgnoreCase("")) {
			schoolNameValueUpdate = new AttributeValueUpdate().withAction(AttributeAction.DELETE);
		} else {
			AttributeValue schoolNameAttributeValue = new AttributeValue().withS(schoolName);
			schoolNameValueUpdate = new AttributeValueUpdate().withAction(AttributeAction.PUT).withValue(
					schoolNameAttributeValue);
		}

		if (memo == null || memo.equalsIgnoreCase("")) {
			memoValueUpdate = new AttributeValueUpdate().withAction(AttributeAction.DELETE);
		} else {
			AttributeValue memoAttributeValue = new AttributeValue().withS(memo);
			memoValueUpdate = new AttributeValueUpdate().withAction(AttributeAction.PUT).withValue(memoAttributeValue);
		}

		UpdatePointRequest updatePointRequest = new UpdatePointRequest(geoPoint, rangeKeyAttributeValue);
		updatePointRequest.getUpdateItemRequest().addAttributeUpdatesEntry("schoolName", schoolNameValueUpdate);
		updatePointRequest.getUpdateItemRequest().addAttributeUpdatesEntry("memo", memoValueUpdate);

		UpdatePointResult updatePointResult = geoDataManager.updatePoint(updatePointRequest);

		printUpdatePointResult(updatePointResult, out);
	}

	private void printUpdatePointResult(UpdatePointResult updatePointResult, PrintWriter out)
			throws JsonParseException, IOException {

		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("action", "update-point");

		out.println(mapper.writeValueAsString(jsonMap));
		out.flush();
	}

	private void queryRectangle(JSONObject requestObject, PrintWriter out) throws IOException, JSONException {
		GeoPoint minPoint = new GeoPoint(requestObject.getDouble("minLat"), requestObject.getDouble("minLng"));
		GeoPoint maxPoint = new GeoPoint(requestObject.getDouble("maxLat"), requestObject.getDouble("maxLng"));
		
		List<String> attributesToGet = new ArrayList<String>();
		attributesToGet.add(config.getRangeKeyAttributeName());
		attributesToGet.add(config.getGeoJsonAttributeName());
		attributesToGet.add("schoolName");

		QueryRectangleRequest queryRectangleRequest = new QueryRectangleRequest(minPoint, maxPoint);
		queryRectangleRequest.getQueryRequest().setAttributesToGet(attributesToGet);
		QueryRectangleResult queryRectangleResult = geoDataManager.queryRectangle(queryRectangleRequest);

		printGeoQueryResult(queryRectangleResult, out);
	}

	private void queryRadius(JSONObject requestObject, PrintWriter out) throws IOException, JSONException {
		GeoPoint centerPoint = new GeoPoint(requestObject.getDouble("lat"), requestObject.getDouble("lng"));
		double radiusInMeter = requestObject.getDouble("radiusInMeter");
		
		List<String> attributesToGet = new ArrayList<String>();
		attributesToGet.add(config.getRangeKeyAttributeName());
		attributesToGet.add(config.getGeoJsonAttributeName());
		attributesToGet.add("schoolName");

		QueryRadiusRequest queryRadiusRequest = new QueryRadiusRequest(centerPoint, radiusInMeter);
		queryRadiusRequest.getQueryRequest().setAttributesToGet(attributesToGet);
		QueryRadiusResult queryRadiusResult = geoDataManager.queryRadius(queryRadiusRequest);

		printGeoQueryResult(queryRadiusResult, out);
	}

	private void printGeoQueryResult(GeoQueryResult geoQueryResult, PrintWriter out) throws JsonParseException,
			IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		List<Map<String, String>> resultArray = new ArrayList<Map<String, String>>();

		for (Map<String, AttributeValue> item : geoQueryResult.getItem()) {
			Map<String, String> itemMap = new HashMap<String, String>();

			String geoJsonString = item.get(config.getGeoJsonAttributeName()).getS();
			JsonParser jsonParser = factory.createJsonParser(geoJsonString);
			JsonNode jsonNode = mapper.readTree(jsonParser);

			double latitude = jsonNode.get("coordinates").get(0).getDoubleValue();
			double longitude = jsonNode.get("coordinates").get(1).getDoubleValue();
			String rangeKey = item.get(config.getRangeKeyAttributeName()).getS();
			String schoolName = "";
			if (item.containsKey("schoolName")) {
				schoolName = item.get("schoolName").getS();
			}

			itemMap.put("latitude", Double.toString(latitude));
			itemMap.put("longitude", Double.toString(longitude));
			itemMap.put("rangeKey", rangeKey);
			itemMap.put("schoolName", schoolName);

			resultArray.add(itemMap);
		}

		jsonMap.put("action", "query");
		jsonMap.put("result", resultArray);

		out.println(mapper.writeValueAsString(jsonMap));
		out.flush();
	}

	private void deletePoint(JSONObject requestObject, PrintWriter out) throws IOException, JSONException {
		GeoPoint geoPoint = new GeoPoint(requestObject.getDouble("lat"), requestObject.getDouble("lng"));
		AttributeValue rangeKeyAttributeValue = new AttributeValue().withS(requestObject.getString("rangeKey"));

		DeletePointRequest deletePointRequest = new DeletePointRequest(geoPoint, rangeKeyAttributeValue);
		DeletePointResult deletePointResult = geoDataManager.deletePoint(deletePointRequest);

		printDeletePointResult(deletePointResult, out);
	}

	private void printDeletePointResult(DeletePointResult deletePointResult, PrintWriter out)
			throws JsonParseException, IOException {

		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("action", "delete-point");

		out.println(mapper.writeValueAsString(jsonMap));
		out.flush();
	}

	public void destroy() {
		config.getExecutorService().shutdownNow();
	}
}