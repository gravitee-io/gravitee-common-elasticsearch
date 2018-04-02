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

import io.gravitee.elasticsearch.model.SearchResponse;
import io.gravitee.elasticsearch.utils.Type;
import io.gravitee.repository.analytics.AnalyticsException;
import io.gravitee.repository.analytics.query.Query;
import io.gravitee.repository.analytics.query.count.CountQuery;
import io.gravitee.repository.analytics.query.count.CountResponse;
import io.reactivex.Single;

/**
 * Commmand used to handle CountQuery
 * 
 * @author Guillaume Waignier (Zenika)
 * @author Sebastien Devaux (Zenika)
 *
 */
public class CountQueryCommand extends AstractElasticsearchQueryCommand<CountResponse> {

	private final static String TEMPLATE = "count.ftl";

	@Override
	public Class<? extends Query<CountResponse>> getSupportedQuery() {
		return CountQuery.class;
	}

	@Override
	public CountResponse executeQuery(Query<CountResponse> query) throws AnalyticsException {
		final CountQuery countQuery = (CountQuery) query;
		
		final String sQuery = this.createQuery(TEMPLATE, query);
		
		try {
			final Single<SearchResponse> result;

			if (countQuery.timeRange() != null) {
				final Long from = countQuery.timeRange().range().from();
				final Long to = countQuery.timeRange().range().to();

				result = this.client.search(
						this.indexNameGenerator.getIndexName(Type.REQUEST, from, to),
						Type.REQUEST.getType(),
						sQuery);
			} else {
				result = this.client.search(
						this.indexNameGenerator.getTodayIndexName(Type.REQUEST),
						Type.REQUEST.getType(),
						sQuery);
			}

			SearchResponse searchResponse = result.blockingGet();
			return this.toCountResponse(searchResponse);
		} catch (final Exception eex) {
			logger.error("Impossible to perform GroupByQuery", eex);
			throw new AnalyticsException("Impossible to perform GroupByQuery", eex);
		}
	}

	private CountResponse toCountResponse(final SearchResponse response) {
		final CountResponse countResponse = new CountResponse();
		countResponse.setCount(response.getSearchHits().getTotal());
		return countResponse;
	}
}
