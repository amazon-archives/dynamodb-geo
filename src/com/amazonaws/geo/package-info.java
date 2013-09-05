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
/**
 * <b>Geo Library for Amazon DynamoDB</b>
 * <p>
 * This server library will enable you to create, retrieve, and query for geo-spatial data records in DynamoDB. Geo data
 * is popular in mobile apps, and along with this library we have sample mobile projects for iOS which
 * demonstrate usage.
 * </p>
 * <b>Geo-spatial data in a nutshell</b>
 * <p>
 * A Geo-spatial data record is simply one which is tagged with a latitude and a longitude, which correspond to that
 * record's "place" on the planet Earth. Many mobile apps which run on smart phones generate geo data records by
 * accessing the GPS to determine the current location, then pass the lat/lng pair to a data storage layer for retrieval
 * in the future. Mobile apps also commonly access geo data records which are "nearby". This is done by performing a
 * query for all the geo data records which are within a radius or a bounding box around the current location of the
 * device.
 * </p>
 * <p>
 * Storing, retrieving, and querying for data records which have a physical location has always been possible with
 * DynamoDB, but it required careful design of hash keys, ranges, and indexes, as well as associated code for
 * calculating a point's position "inside or outside" a given grid which overlays the globe. Our new library makes it
 * very easy.
 * </p>
 * <b>Major components:</b>
 * <ul>
 * <li>Geo Library for Amazon DynamoDB: Java library which provides a high-level interface to DynamoDB-backed data
 * storage of geo objects. This library can be added to your existing server backend. For more help getting started, we
 * include a reference sample Java server for AWS ElasticBeanstalk.</li>
 * <li>
 * DynamoDB tables: you create and configure these tables to store your geo records. The library takes care of
 * determining the proper hash keys, range keys, and indexes. These keys and indexes are how queries can "find" the
 * records which are within a physical distance of a given starting point.</li>
 * <li>
 * Client code: interacting with the server library can occur any way you prefer. We have included reference samples of
 * mobile apps for iOS and Android which demonstrate how to gather the lat/lng from the device and send it to the
 * library, as well as how to query for geo records and display them on a map view.</li>
 * </ul>
 * <b>How does a query work?</b>
 * <p>
 * We overlay a virtual "grid" over the planet earth. Each grid cell has a corresponding "address", derived by location.
 * When geo points are inserted to DynamoDB, a "geohash" is constructed, which locates the data record into the correct
 * grid cell. Geohashes also attempt to preserve the proximity of nearby points. Using local secondary indexes, the
 * geohash is stored in DynamoDB along with the data record.
 * </p>
 * <p>
 * For querying, you provide either a center point lat/lng and a radial distance, or the coordinates of a bounding box.
 * The library uses this input to determine which grid cells are "candidates" for returning geo records from DynamoDB.
 * Those cells are queried by geohash - again, a local secondary index on the DynamoDB table - to return the candidate
 * geo records. Some cells contain lots of points, some contain none, depending on what points were originally stored in
 * the table. Next, the library post-processes those records, filtering out the ones which are outside of the
 * originally-provided input (the bounding box or radius). The final list of matching geo records are returned to the
 * client.
 * </p>
 * 
 * @since 1.0
 * @version 1.0
 * */
package com.amazonaws.geo;