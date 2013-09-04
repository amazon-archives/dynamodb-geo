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

import com.amazonaws.geo.GeoDataManagerConfiguration;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

/**
 * Utility class.
 * */
public class GeoTableUtil {

	/**
	 * <p>
	 * Construct a create table request object based on GeoDataManagerConfiguration. The users can update any aspect of
	 * the request and call it.
	 * </p>
	 * Example:
	 * 
	 * <pre>
	 * AmazonDynamoDBClient ddb = new AmazonDynamoDBClient(new ClasspathPropertiesFileCredentialsProvider());
	 * Region usWest2 = Region.getRegion(Regions.US_WEST_2);
	 * ddb.setRegion(usWest2);
	 * 
	 * CreateTableRequest createTableRequest = GeoTableUtil.getCreateTableRequest(config);
	 * CreateTableResult createTableResult = ddb.createTable(createTableRequest);
	 * </pre>
	 * 
	 * @return Generated create table request.
	 */
	public static CreateTableRequest getCreateTableRequest(GeoDataManagerConfiguration config) {
		CreateTableRequest createTableRequest = new CreateTableRequest()
				.withTableName(config.getTableName())
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(5L))
				.withKeySchema(
						new KeySchemaElement().withKeyType(KeyType.HASH).withAttributeName(
								config.getHashKeyAttributeName()),
						new KeySchemaElement().withKeyType(KeyType.RANGE).withAttributeName(
								config.getRangeKeyAttributeName()))
				.withAttributeDefinitions(
						new AttributeDefinition().withAttributeType(ScalarAttributeType.N).withAttributeName(
								config.getHashKeyAttributeName()),
						new AttributeDefinition().withAttributeType(ScalarAttributeType.S).withAttributeName(
								config.getRangeKeyAttributeName()),
						new AttributeDefinition().withAttributeType(ScalarAttributeType.N).withAttributeName(
								config.getGeohashAttributeName()))
				.withLocalSecondaryIndexes(
						new LocalSecondaryIndex()
								.withIndexName(config.getGeohashIndexName())
								.withKeySchema(
										new KeySchemaElement().withKeyType(KeyType.HASH).withAttributeName(
												config.getHashKeyAttributeName()),
										new KeySchemaElement().withKeyType(KeyType.RANGE).withAttributeName(
												config.getGeohashAttributeName()))
								.withProjection(new Projection().withProjectionType(ProjectionType.ALL)));

		return createTableRequest;
	}
}
