package com.madadipouya.telegram.corona.virus.integration.mathdro.impl;

import com.madadipouya.telegram.corona.virus.integration.mathdro.MathdroIntegration;
import com.madadipouya.telegram.corona.virus.integration.mathdro.remote.response.CoronaStatistics;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.madadipouya.telegram.corona.general.config.CacheManagerConfig.CORONA_ALL_STATISTICS_CACHE;
import static com.madadipouya.telegram.corona.general.config.CacheManagerConfig.CORONA_ALL_STATISTICS_FLAT_CACHE;
import static com.madadipouya.telegram.corona.general.config.CacheManagerConfig.CORONA_COUNTRY_STATISTICS_CACHE;
import static com.madadipouya.telegram.corona.utils.StringUtils.getOrDefaultEmpty;

/*
 * This file is part of COVID-19-Telegram-bot.
 *
 * COVID-19-Telegram-bot is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * COVID-19-Telegram-bot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.  <http://www.gnu.org/licenses/>
 *
 * Author(s):
 *
 * © 2020 Kasra Madadipouya <kasra@madadipouya.com>
 */

@Service
public class DefaultMathdroIntegration implements MathdroIntegration {

    private static final String API_URL = "https://covid19.mathdro.id/api/confirmed";

    private static final Set<String> AUTONOMOUS_STATES = Set.of("Hong Kong", "Macau");

    private final RestTemplate restTemplate;

    @Autowired
    private MathdroIntegration self;

    public DefaultMathdroIntegration(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Cacheable(value = CORONA_ALL_STATISTICS_FLAT_CACHE)
    public List<String> getLatestCoronaStatisticsFlat() {
        return self.getLatestCoronaStatistics().stream().map(CoronaStatistics::getAsString).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = CORONA_ALL_STATISTICS_CACHE)
    public List<CoronaStatistics> getLatestCoronaStatistics() {
        ResponseEntity<List<CoronaStatistics>> responseEntity = restTemplate.exchange(API_URL, HttpMethod.GET,
                null, new ParameterizedTypeReference<List<CoronaStatistics>>() {
                });
        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            return List.of();
        }
        Map<String, CoronaStatistics> results = new LinkedHashMap<>();
        List<CoronaStatistics> coronaStatistics = responseEntity.getBody();
        coronaStatistics.forEach(coronaRecord -> {
            if (AUTONOMOUS_STATES.contains(getOrDefaultEmpty(coronaRecord.getState()))) {
                results.put(coronaRecord.getState(), new CoronaStatistics(coronaRecord.getState(),
                        coronaRecord.getCountryCode(), coronaRecord.getConfirmed(), coronaRecord.getRecovered(),
                        coronaRecord.getDeaths()));
            } else {
                results.merge(coronaRecord.getCountry(), coronaRecord, (v1, v2) ->
                        new CoronaStatistics(v1.getCountry(), v1.getCountryCode(),
                                v1.getConfirmed() + v2.getConfirmed(),
                                v1.getRecovered() + v2.getRecovered(),
                                v1.getDeaths() + v2.getDeaths()));
            }
        });
        return results.values().stream()
                .sorted((o1, o2) -> o1.getConfirmed() < o2.getConfirmed() ? -1 : o1 == o2 || o1.getConfirmed() == o2.getConfirmed() ? 0 : 1)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = CORONA_COUNTRY_STATISTICS_CACHE, key = "{ #countryCode }")
    public String getLatestCoronaStatisticsByCountry(String countryCode) {
        return self.getLatestCoronaStatistics().stream().filter(r -> StringUtils.equalsAnyIgnoreCase(countryCode, r.getCountryCode()))
                .findFirst().map(CoronaStatistics::getAsString).orElse(StringUtils.EMPTY);
    }
}
