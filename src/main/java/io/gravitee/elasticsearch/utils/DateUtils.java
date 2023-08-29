/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.elasticsearch.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class used to compute date format for Elasticsearch indexes.
 *
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public final class DateUtils {

    /**
     * Date format for Elasticsearch indexes.
     */
    private static final DateTimeFormatter ES_DAILY_INDICE = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter ES_MONTHLY_INDICE = DateTimeFormatter.ofPattern("yyyy.MM.*");

    private DateUtils() {}

    /**
     * Compute all suffix index names corresponding to Elasticsearch indexes between from and to.
     * Used default system timezone.
     * @param from timestamp for the start range time
     * @param to timestamp for the end range time
     * @return
     */
    public static List<String> rangedIndices(final long from, final long to) {
        final List<String> indices = new ArrayList<>();

        LocalDate start = Instant.ofEpochMilli(from).atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate stop = Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDate();
        YearMonth startYearMonth = YearMonth.from(start);
        YearMonth stopYearMonth = YearMonth.from(stop);

        // if the start date and the stop date are in the same month
        if (startYearMonth.getMonth().equals(stopYearMonth.getMonth())) {
            // if we selected an entire month then add the monthly indice otherwise loop on daily indices
            if (startYearMonth.atDay(1).equals(start) && stopYearMonth.atEndOfMonth().equals(stop)) {
                indices.add(ES_MONTHLY_INDICE.format(startYearMonth));
            } else {
                while (start.isBefore(stop) || start.isEqual(stop)) {
                    indices.add(ES_DAILY_INDICE.format(start));
                    start = start.plusDays(1);
                }
            }
        } else {
            if (startYearMonth.atDay(1).equals(start)) {
                // loop on monthly indices until the last month
                while (startYearMonth.isBefore(stopYearMonth)) {
                    indices.add(ES_MONTHLY_INDICE.format(startYearMonth));
                    startYearMonth = startYearMonth.plusMonths(1);
                }
            } else {
                // loop on daily indices until the end of the month
                while (start.isBefore(startYearMonth.atEndOfMonth()) || start.isEqual(startYearMonth.atEndOfMonth())) {
                    indices.add(ES_DAILY_INDICE.format(start));
                    start = start.plusDays(1);
                }

                // add other monthly indices until the last month
                startYearMonth = startYearMonth.plusMonths(1);
                while (startYearMonth.isBefore(stopYearMonth)) {
                    indices.add(ES_MONTHLY_INDICE.format(startYearMonth));
                    startYearMonth = startYearMonth.plusMonths(1);
                }
            }

            // if end date is the last day of the month then add the monthly indices
            if (stopYearMonth.atEndOfMonth().isEqual(stop)) {
                indices.add(ES_MONTHLY_INDICE.format(stopYearMonth));
            } else { // loop on daily indices until the end date of the last month
                LocalDate day = stopYearMonth.atDay(1);
                while (day.isBefore(stop) || day.isEqual(stop)) {
                    indices.add(ES_DAILY_INDICE.format(day));
                    day = day.plusDays(1);
                }
            }
        }
        return indices;
    }
}
