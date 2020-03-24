package com.madadipouya.telegram.corona.virus.integration.mathdro.remote.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.madadipouya.telegram.corona.utils.NumberUtils.formatThreeDecimal;

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

public class CoronaStatistics {

    @JsonProperty("countryRegion")
    private String country;

    @JsonProperty("provinceState")
    private String state;

    private int confirmed;

    private int recovered;

    private int deaths;

    @JsonProperty("iso2")
    private String countryCode;

    public CoronaStatistics(String country, String countryCode, int confirmed, int recovered, int deaths) {
        this.country = country;
        this.countryCode = countryCode;
        this.confirmed = confirmed;
        this.recovered = recovered;
        this.deaths = deaths;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public int getConfirmed() {
        return confirmed;
    }

    public int getRecovered() {
        return recovered;
    }

    public int getDeaths() {
        return deaths;
    }

    public String getAsString() {
        return String.format("%s, %s, %s, %s", country, formatThreeDecimal(confirmed),
                formatThreeDecimal(deaths), formatThreeDecimal(recovered));
    }
}
