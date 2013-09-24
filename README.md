#Geo Library for Amazon DynamoDB

The Geo Library for [Amazon DynamoDB][dynamodb] enables Java developers to easily create and query geospatial data. The library takes care of managing the geohash indexes required for fast and efficient execution of location-based queries over a table of items representing points of interest - latitude/longitude pairs.

Along with this library we provide sample applications demonstrating usage of the library for a cloud-backed mobile app development scenario. You can get up and running quickly with a sample [AWS ElasticBeanstalk][eb] application and a sample iOS project.

##API Reference
[Geo Library for Amazon DynamoDB Javadoc][geo-library-javadoc]

##Features
* **Box Queries:** Return all of the items that fall within a pair of geo points that define a rectangle as projected onto a sphere.
* **Radius Queries:** Return all of the items that are within a given radius of a geo point.
* **Basic CRUD Operations:** Create, retrieve, update, and delete geospatial data items.
* **Easy Integration:** Adds functionality to the AWS SDK for Java in your server application.
* **Customizable:** Access to raw request and result objects from the AWS SDK for Java.

##Getting Started
###Setup Environment
1. **Sign up for AWS** - Before you begin, you need an AWS account. Please see the [AWS Account and Credentials][docs-signup] section of the developer guide for information about how to create an AWS account and retrieve your AWS credentials.
2. **Minimum Java requirements** - To run the SDK you will need **Java 1.6+**. We strongly recommend Java 6 Update 24 at the minimum to mitigate the parseDouble DoS attacks. For more information about the requirements and optimum settings for the SDK, please see the [Java Development Environment][docs-signup] section of the developer guide.
3. **Minimum iOS requirements** - The sample iOS app supports iOS 5 and above.
4. **Download Geo Library for Amazon DynamoDB** - To download the code from GitHub, simply clone the repository by typing: `git clone https://github.com/awslabs/dynamodb-geo`.

###Run the Sample Server App
1. Go to [AWS Management Console][management-console] and select **ElasticBeanstalk**.
2. Click **Create a New Application**.
3. Specify **Application name** and click **Create**.
4. Make sure **Launch a new environment running this application** is selected.
5. Select **Tomcat** for **Predefined configuration**. **Environment type** can be either **Loadbalancing, autoscaling** or **Single instance**. Then click **Continue**.
6. Select **Upload your own** for **Source**. Click **Browse** and select `/samples/dynamodb-geo-server/dynamodb-geo-server.war`. Then click **Continue**.
7. Choose your **Environment name** and **Environment URL**. Then click **Continue**.
8. Make sure **Create an RDS DB Instance with this environment** is **NOT** checked. Then click **Continue**.
9. Accept the default values for **Configuration Details** and click **Continue**.
10. Review the information and click **Create**.
11. Wait until **Health** becomes **Green**. This may take a few minutes.
12. Select **Configuration** > **Software Configuration**.
13. Under **Environment Properties**, type your **AWS_ACCESS_KEY_ID** and **AWS_SECRET_KEY**.
14. Type your DynamoDB table name for **PARAM1** (e.g. `geo-test`). Make sure you do **NOT** already have a table with the same name.
15. Type your DynamoDB region name for **PARAM2** (e.g. `us-west-2`). For the full list of regions, please read [Regions and Endpoints][regions-endpoints].
16. Click **Save**.
17. Click on **Dashboard** and wait until **Health** becomes **Green**. This may take a few minutes.
18. Open your URL (e.g. `http://YOUR-ENDPOINT.elasticbeanstalk.com`). **NOTE:** For security reasons, we strongly encourage developers to use https endpoints on ElasticBeanstalk. Please read [Configuring HTTPS for your AWS Elastic Beanstalk Environment][eb-https] for more details.
19. The status on the website will change as follows: **preparing to start** > **creating a table** > **inserting test data into the table** > **running**.
20. Wait until the website says **Congratulations! Geo Library for Amazon DynamoDB Sample Server is running**. This may take 10 to 20 minutes.

###Run the Sample iOS App
1. Open the Xcode project under `samples/dynamodb-geo-ios/dynamodb-geo-ios.xcodeproj`.
2. Open **AWSConstants.m**.
3. Update **YOUR-ENVIRONMENT** part of `http://YOUR-ENVIRONMENT.elasticbeanstalk.com/dynamodb-geo` with your actual environment URL.
4. Click **Run** on Xcode to run the sample iOS app.

##Building From Source
Once you check out the code from GitHub, you can build it using **Maven**: `mvn package`

##Limitations

###Java focused
Internally, Geo Library uses the [S2 Geometry Library](https://code.google.com/p/s2-geometry-library-java/) for spherical math, and the library is available only in Java and C++. For now, we are focusing on Java, and we don't have short term plans to port Geo Library for other languages.

###No composite key support
Currently, the library does not support composite keys. You may want to add tags such as restaurant, bar, and coffee shop, and search locations of a specific category; however, it is currently not possible. You need to create a table for each tag and store the items separately.

###Queries retrieve all paginated data
Although low level [DynamoDB Query][dynamodb-query] requests return paginated results, this library automatically pages through the entire result set. When querying a large area with many points, a lot of Read Capacity Units may be consumed.

###More Read Capacity Units
The library retrieves candidate Geo points from the cells that intersect the requested bounds. The library then post-processes the candidate data, filtering out the specific points that are outside the requested bounds. Therefore, the consumed Read Capacity Units will be higher than the final results dataset.

###High memory consumption
Because all paginated `Query` results are loaded into memory and processed, it may consume substantial amounts of memory for large datasets.

###The server is essential
Because Geo Library calls multiple DynamoDB `Query` requests and processes the results in memory, it is not suitable for mobile device use. You should maintain a Java server, and use the library on the server.

###Dataset density limitation
The Geohash used in this library is roughly centimeter precision. Therefore, the library is not suitable if your dataset has much higher density.

##Reference

###Amazon DynamoDB
* [Amazon DynamoDB][dynamodb]
* [Amazon DynamoDB Forum][dynamodb-forum]

###AWS ElasticBeanstalk
* [AWS ElasticBeanstalk][eb]
* [AWS ElasticBeanstalk Forum][eb-forum]

###Mobile Development
* [AWS Mobile Developer Center][mobiledev]
* [Mobile Development AWS Blog][mobile-sdk-blog]
* [Mobile Development Forum][mobile-sdk-forum]

###Java Development
* [AWS SDK for Java][java-sdk-website]
* [AWS SDK for Java API Reference][docs-api]
* [AWS SDK for Java Developer Guide][docs-guide]
* [Java Development AWS Blog][java-sdk-blog]
* [Java Development Forum][java-sdk-forum]

[dynamodb]: http://aws.amazon.com/dynamodb
[dynamodb-forum]: https://forums.aws.amazon.com/forum.jspa?forumID=131
[docs-api]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/index.html
[docs-guide]: http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/welcome.html
[java-sdk-forum]: http://developer.amazonwebservices.com/connect/forum.jspa?forumID=70
[java-sdk-website]: http://aws.amazon.com/sdkforjava
[java-sdk-blog]: https://java.awsblog.com/
[mobile-sdk-forum]: https://forums.aws.amazon.com/forum.jspa?forumID=88
[mobile-sdk-blog]: http://mobile.awsblog.com/
[docs-signup]: http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/java-dg-setup.html
[management-console]: https://console.aws.amazon.com/console/home
[regions-endpoints]: http://docs.aws.amazon.com/general/latest/gr/rande.html
[eb-https]: http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/configuring-https.html
[eb]: http://aws.amazon.com/elasticbeanstalk
[eb-forum]: https://forums.aws.amazon.com/forum.jspa?forumID=86
[mobiledev]: https://aws.amazon.com/mobile/
[geo-library-javadoc]: http://awslabs.github.io/dynamodb-geo/
[dynamodb-query]: http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Query.html