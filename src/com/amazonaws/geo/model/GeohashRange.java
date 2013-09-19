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

package com.amazonaws.geo.model;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.geo.GeoDataManagerConfiguration;
import com.amazonaws.geo.s2.internal.S2Manager;

public class GeohashRange {

	private long rangeMin;
	private long rangeMax;

	public GeohashRange(long range1, long range2) {
		this.rangeMin = Math.min(range1, range2);
		this.rangeMax = Math.max(range1, range2);
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

	/*
	 * Try to split the range to multiple ranges based on the hash key.
	 * 
	 * e.g., for the following range:
	 * 
	 * min: 123456789
	 * max: 125678912
	 * 
	 * when the hash key length is 3, we want to split the range to:
	 * 
	 * 1
	 * min: 123456789
	 * max: 123999999
	 * 
	 * 2
	 * min: 124000000
	 * max: 124999999
	 * 
	 * 3
	 * min: 125000000
	 * max: 125678912
	 * 
	 * For this range:
	 * 
	 * min: -125678912
	 * max: -123456789
	 * 
	 * we want:
	 * 
	 * 1
	 * min: -125678912
	 * max: -125000000
	 * 
	 * 2
	 * min: -124999999
	 * max: -124000000
	 * 
	 * 3
	 * min: -123999999
	 * max: -123456789
	 */
	public List<GeohashRange> trySplit(int hashKeyLength) {
		List<GeohashRange> result = new ArrayList<GeohashRange>();

		long minHashKey = S2Manager.generateHashKey(rangeMin, hashKeyLength);
		long maxHashKey = S2Manager.generateHashKey(rangeMax, hashKeyLength);

		long denominator = (long) Math.pow(10, String.valueOf(rangeMin).length() - String.valueOf(minHashKey).length());

		if (minHashKey == maxHashKey) {
			result.add(this);
		} else {
			for (long l = minHashKey; l <= maxHashKey; l++) {
				if (l > 0) {
					result.add(new GeohashRange(l == minHashKey ? rangeMin : l * denominator,
							l == maxHashKey ? rangeMax : (l + 1) * denominator - 1));
				} else {
					result.add(new GeohashRange(l == minHashKey ? rangeMin : (l - 1) * denominator + 1,
							l == maxHashKey ? rangeMax : l * denominator));
				}
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
