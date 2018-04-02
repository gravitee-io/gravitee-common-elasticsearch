/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.repository.elasticsearch.analytics.query;

import com.fasterxml.jackson.databind.JsonNode;
import io.gravitee.elasticsearch.model.Aggregation;
import io.gravitee.elasticsearch.model.SearchResponse;
import io.gravitee.elasticsearch.utils.Type;
import io.gravitee.repository.analytics.AnalyticsException;
import io.gravitee.repository.analytics.query.AggregationType;
import io.gravitee.repository.analytics.query.DateHistogramQuery;
import io.gravitee.repository.analytics.query.Query;
import io.gravitee.repository.analytics.query.response.histogram.Bucket;
import io.gravitee.repository.analytics.query.response.histogram.Data;
import io.gravitee.repository.analytics.query.response.histogram.DateHistogramResponse;
import io.reactivex.Single;

import java.util.*;

/**
 * Commmand used to handle DateHistogramQuery
 * @author Guillaume Waignier (Zenika)
 * @author Sebastien Devaux (Zenika)
 *
 */
public class DateHistogramQueryCommand extends AstractElasticsearchQueryCommand<DateHistogramResponse> {

	private final static String TEMPLATE = "dateHistogram.ftl";
	
	@Override
	public Class<? extends Query<DateHistogramResponse>> getSupportedQuery() {
		return DateHistogramQuery.class;
	}

	@Override
	public DateHistogramResponse executeQuery(Query<DateHistogramResponse> query) throws AnalyticsException {
		final DateHistogramQuery dateHistogramQuery = (DateHistogramQuery) query;

		final String sQuery = this.createQuery(TEMPLATE, query);
		final Long from = dateHistogramQuery.timeRange().range().from();
		final Long to = dateHistogramQuery.timeRange().range().to();

		try {
			final Single<SearchResponse> result = this.client.search(
					this.indexNameGenerator.getIndexName(Type.REQUEST, from, to),
					Type.REQUEST.getType(),
					sQuery);
			return this.toDateHistogramResponse(result.blockingGet(), dateHistogramQuery);
		} catch (final Exception eex) {
			logger.error("Impossible to perform DateHistogramQuery", eex);
			throw new AnalyticsException("Impossible to perform DateHistogramQuery", eex);
		}
	}

	private DateHistogramResponse toDateHistogramResponse(final SearchResponse response,
			final DateHistogramQuery query) {
		final DateHistogramResponse dateHistogramResponse = new DateHistogramResponse();

		if (response.getAggregations() == null) {
			return dateHistogramResponse;
		}

		// Prepare data
		final Map<String, Bucket> fieldBuckets = new HashMap<>();

		final Aggregation dateHistogram = response.getAggregations().get("by_date");
		for (JsonNode dateBucket : dateHistogram.getBuckets()) {
			final long keyAsDate = dateBucket.get("key").asLong();
			dateHistogramResponse.timestamps().add(keyAsDate);

			final Iterator<String> fieldNamesInDateBucket = dateBucket.fieldNames();
			while (fieldNamesInDateBucket.hasNext()) {
				final String fieldNameInDateBucket = fieldNamesInDateBucket.next();
				this.handleSubAggregation(fieldBuckets, fieldNameInDateBucket, dateBucket, keyAsDate);
			}
		}

		if (!query.aggregations().isEmpty()) {
			query.aggregations().forEach(aggregation -> {
				String key = aggregation.type().name().toLowerCase() + '_' + aggregation.field();
				if (aggregation.type() == AggregationType.FIELD) {
					key = "by_" + aggregation.field();
				}

				dateHistogramResponse.values().add(fieldBuckets.get(key));
			});
		}
		return dateHistogramResponse;
	}

	private void handleSubAggregation(final Map<String, Bucket> fieldBuckets, final String fieldNameInDateBucket,
			final JsonNode dateBucket, final long keyAsDate) {
		if (!fieldNameInDateBucket.startsWith("by_") && !fieldNameInDateBucket.startsWith("avg_")
				&& !fieldNameInDateBucket.startsWith("min_") && !fieldNameInDateBucket.startsWith("max_")) {
			return;
		}

		final String kindAggregation = fieldNameInDateBucket.split("_")[0];
		final String fieldName = fieldNameInDateBucket.split("_")[1];

		Bucket fieldBucket = fieldBuckets.get(fieldNameInDateBucket);
		if (fieldBucket == null) {
			fieldBucket = new Bucket(fieldNameInDateBucket, fieldName);
			fieldBuckets.put(fieldNameInDateBucket, fieldBucket);
		}

		final Map<String, List<Data>> bucketData = fieldBucket.data();
		List<Data> data;

		switch (kindAggregation) {
		case "by":
			for (final JsonNode termBucket : dateBucket.get(fieldNameInDateBucket).get("buckets")) {

				final String keyAsString = termBucket.get("key").asText();
				data = bucketData.get(keyAsString);
				if (data == null) {
					data = new ArrayList<>();
					bucketData.put(keyAsString, data);
				}
				data.add(new Data(keyAsDate, termBucket.get("doc_count").asLong()));
			}
			break;
		case "min":
		case "max":
		case "avg":
			final JsonNode numericBucket = dateBucket.get(fieldNameInDateBucket);
			if (numericBucket.get("value") != null && numericBucket.get("value").isNumber()) {
				final double value = numericBucket.get("value").asDouble();
				data = bucketData.get(fieldNameInDateBucket);
				if (data == null) {
					data = new ArrayList<>();
					bucketData.put(fieldNameInDateBucket, data);
				}
				data.add(new Data(keyAsDate, (long) value));
			}
			break;
		}
	}
}
