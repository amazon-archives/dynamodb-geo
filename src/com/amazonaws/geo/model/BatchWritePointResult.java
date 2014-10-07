package com.amazonaws.geo.model;

import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;

public class BatchWritePointResult {
	private BatchWriteItemResult batchWriteItemResult;
	
	public BatchWritePointResult(BatchWriteItemResult batchWriteItemResult) {
		this.batchWriteItemResult = batchWriteItemResult;
	}

	public BatchWriteItemResult getBatchWriteItemResult() {
		return batchWriteItemResult;
	}
}
