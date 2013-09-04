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

package com.amazonaws.geo.model;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.geo.GeoDataManagerConfiguration;

public class GeohashRange {

	private long rangeMin;
	private long rangeMax;

	public GeohashRange(long rangeMin, long rangeMax) {
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
	}

	public boolean tryMerge(GeohashRange range) {
		if (range.getRangeMin() - this.rangeMax <= GeoDataManagerConfiguration.MERGE_THRESHOLD
				&& range.getRangeMin() - this.rangeMax > 0) {
			this.rangeMax = range.getRangeMax();
			return true;
		}

		if (this.rangeMin - range.getRangeMax() <= GeoDataManagerConfiguration.MERGE_THRESHOLD
				&& this.rangeMin - range.getRangeMax() > 0) {
			this.rangeMin = range.getRangeMin();
			return true;
		}

		return false;
	}

	public List<GeohashRange> trySplit(int hashKeyLength) {
		List<GeohashRange> result = new ArrayList<GeohashRange>();

		long denominator = (long) Math.pow(10, GeoDataManagerConfiguration.GEOHASH_LENGTH - hashKeyLength);
		long minHashKey = (long) (rangeMin / denominator);
		long maxHashKey = (long) (rangeMax / denominator);

		if (minHashKey == maxHashKey) {
			result.add(this);
		} else {
			for (long l = minHashKey; l <= maxHashKey; l++) {
				result.add(new GeohashRange(l == minHashKey ? rangeMin : l * denominator, l == maxHashKey ? rangeMax
						: (l + 1) * denominator - 1));
			}
		}

		return result;
	}

	public long getRangeMin() {
		return rangeMin;
	}

	public void setRangeMin(long rangeMin) {
		this.rangeMin = rangeMin;
	}

	public long getRangeMax() {
		return rangeMax;
	}

	public void setRangeMax(long rangeMax) {
		this.rangeMax = rangeMax;
	}
}
