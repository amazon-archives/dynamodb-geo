<!--
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
-->

<%@page import="com.amazonaws.geo.server.util.Utilities"%>

<html>
<head>
<title>Geo Library for Amazon DynamoDB Sample Server - Status</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="refresh" content="15">
<meta name="viewport"
	content="width=device-width, minimum-scale=1.0, maximum-scale=1.0">
<link rel="stylesheet" href="css/styles.css" type="text/css"
	media="screen">
<link rel="stylesheet" href="css/styles-mobile.css" type="text/css"
	media="screen">
<link rel="stylesheet" href="css/styles-tablet.css" type="text/css"
	media="screen" title="no title">
</head>

<body class="success">
	<div id="header">
		<h1>Geo Library for Amazon DynamoDB Sample Server</h1>
	</div>

	<div id="body">
		<fieldset>
			<%
				if (Utilities.getInstance().isAccessKeySet() && Utilities.getInstance().isSecretKeySet()
						&& Utilities.getInstance().isTableNameSet() && Utilities.getInstance().isRegionNameSet()) {
					Utilities.getInstance().setupTable();
				}

				if (Utilities.getInstance().getStatus() == Utilities.Status.READY) {
			%>
			<legend>Congratulations!</legend>
			<p class="message">Geo Library for Amazon DynamoDB Sample Server
				is running.</p>
			<%
				} else if (Utilities.getInstance().getStatus() == Utilities.Status.NOT_STARTED) {
			%>
			<p class="message">Geo Library for Amazon DynamoDB Sample Server
				is preparing to start.</p>
			<%
				} else if (Utilities.getInstance().getStatus() == Utilities.Status.CREATING_TABLE) {
			%>
			<p class="message">Geo Library for Amazon DynamoDB Sample Server
				is creating a table. This may take a few minutes. Please wait.</p>
			<%
				} else if (Utilities.getInstance().getStatus() == Utilities.Status.INSERTING_DATA_TO_TABLE) {
			%>
			<p class="message">Geo Library for Amazon DynamoDB Sample Server
				is inserting test data into the table. This may take 10 to 20 minutes. Please wait.</p>
			<%
				}
			%>
		</fieldset>
	</div>

	<%
		if (!Utilities.getInstance().isAccessKeySet()) {
	%>
	<p class="warning">
		Warning: Your <b>AWS_ACCESS_KEY_ID</b> is not properly configured.
	</p>
	<%
		} else if (!Utilities.getInstance().isSecretKeySet()) {
	%>
	<p class="warning">
		Warning: Your <b>AWS_SECRET_KEY</b> is not properly configured.
	</p>
	<%
		} else if (!Utilities.getInstance().isTableNameSet()) {
	%>
	<p class="warning">
		Warning: Your <b>PARAM1</b> is not properly configured. Enter your
		Amazon DynamoDB table name.
	</p>
	<%
		} else if (!Utilities.getInstance().isRegionNameSet()) {
	%>
	<p class="warning">
		Warning: Your <b>PARAM2</b> is not properly configured. Enter your
		Amazon DynamoDB region name (eg. us-west-2).
	</p>
	<%
		}
	%>

	<div id="footer">
		<p class="footnote">Geo Library for Amazon DynamoDB Sample Server</p>
	</div>
</body>

</html>
