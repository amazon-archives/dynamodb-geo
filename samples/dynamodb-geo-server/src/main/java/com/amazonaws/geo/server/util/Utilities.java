package com.amazonaws.geo.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.geo.GeoDataManager;
import com.amazonaws.geo.GeoDataManagerConfiguration;
import com.amazonaws.geo.model.GeoPoint;
import com.amazonaws.geo.model.PutPointRequest;
import com.amazonaws.geo.util.GeoTableUtil;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;

public class Utilities {
	private static Utilities utilities;

	public enum Status {
		NOT_STARTED, CREATING_TABLE, INSERTING_DATA_TO_TABLE, READY
	}

	private Status status = Status.NOT_STARTED;
	private GeoDataManager geoDataManager;

	public static synchronized Utilities getInstance() {
		if (utilities == null) {
			utilities = new Utilities();
		}

		return utilities;
	}

	public Status getStatus() {
		return status;
	}

	public boolean isAccessKeySet() {
		String accessKey = System.getProperty("AWS_ACCESS_KEY_ID");
		return accessKey != null && accessKey.length() > 0;
	}

	public boolean isSecretKeySet() {
		String secretKey = System.getProperty("AWS_SECRET_KEY");
		return secretKey != null && secretKey.length() > 0;
	}

	public boolean isTableNameSet() {
		String tableName = System.getProperty("PARAM1");
		return tableName != null && tableName.length() > 0;
	}

	public boolean isRegionNameSet() {
		String regionName = System.getProperty("PARAM2");
		return regionName != null && regionName.length() > 0;
	}

	public void setupTable() {
		setupGeoDataManager();

		GeoDataManagerConfiguration config = geoDataManager.getGeoDataManagerConfiguration();
		DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(config.getTableName());

		try {
			config.getDynamoDBClient().describeTable(describeTableRequest);

			if (status == Status.NOT_STARTED) {
				status = Status.READY;
			}
		} catch (ResourceNotFoundException e) {
			SchoolDataLoader schoolDataLoader = new SchoolDataLoader();
			schoolDataLoader.start();
		}

	}

	private synchronized void setupGeoDataManager() {
		if (geoDataManager == null) {
			String accessKey = System.getProperty("AWS_ACCESS_KEY_ID");
			String secretKey = System.getProperty("AWS_SECRET_KEY");
			String tableName = System.getProperty("PARAM1");
			String regionName = System.getProperty("PARAM2");

			Region region = Region.getRegion(Regions.fromName(regionName));
			ClientConfiguration clientConfiguration = new ClientConfiguration().withMaxErrorRetry(20);
			AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

			AmazonDynamoDBClient ddb = new AmazonDynamoDBClient(credentials, clientConfiguration);
			ddb.setRegion(region);

			GeoDataManagerConfiguration config = new GeoDataManagerConfiguration(ddb, tableName);
			geoDataManager = new GeoDataManager(config);
		}
	}

	private class SchoolDataLoader extends Thread {
		public void run() {
			status = Status.CREATING_TABLE;

			GeoDataManagerConfiguration config = geoDataManager.getGeoDataManagerConfiguration();

			CreateTableRequest createTableRequest = GeoTableUtil.getCreateTableRequest(config);
			config.getDynamoDBClient().createTable(createTableRequest);

			waitForTableToBeReady();

			insertData();
		}

		private void insertData() {
			status = Status.INSERTING_DATA_TO_TABLE;

			InputStream fis = Utilities.this.getClass().getResourceAsStream("/school_list_wa.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			String line;

			try {
				while ((line = br.readLine()) != null) {
					String[] columns = line.split("\t");
					String schoolId = columns[0];
					String schoolName = columns[1];
					double latitude = Double.parseDouble(columns[2]);
					double longitude = Double.parseDouble(columns[3]);

					GeoPoint geoPoint = new GeoPoint(latitude, longitude);
					AttributeValue rangeKeyValue = new AttributeValue().withS(schoolId);
					AttributeValue schoolNameValue = new AttributeValue().withS(schoolName);

					PutPointRequest putPointRequest = new PutPointRequest(geoPoint, rangeKeyValue);
					putPointRequest.getPutItemRequest().getItem().put("schoolName", schoolNameValue);

					geoDataManager.putPoint(putPointRequest);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					br.close();
					fis.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			status = Status.READY;
		}

		private void waitForTableToBeReady() {
			GeoDataManagerConfiguration config = geoDataManager.getGeoDataManagerConfiguration();

			DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(config.getTableName());
			DescribeTableResult describeTableResult = config.getDynamoDBClient().describeTable(describeTableRequest);

			while (!describeTableResult.getTable().getTableStatus().equalsIgnoreCase("ACTIVE")) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				describeTableResult = config.getDynamoDBClient().describeTable(describeTableRequest);
			}
		}
	}
}
