package com.centurion.centurion_core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TickerTapeStockResponse {

    private Data data;

    @lombok.Data
    public static class Data {
        private List<Result> results;
        private Stats stats;
    }

    @lombok.Data
    public static class Stats {
        private Integer count;
    }

    @lombok.Data
    public static class Result {
        @JsonProperty("sid")
        private String sId;
        private Stock stock;

        @lombok.Data
        public static class Stock {
            private Info info;
            private Ratios advancedRatios;

        }

        @lombok.Data
        public static class Info {
            private String name;
            private String sector;
        }

        @lombok.Data
        public static class Ratios {
            private Double lastPrice;
            @JsonProperty("mrktCapf")
            private Double marketCap;
            @JsonProperty("apef")
            private Double pe;
            @JsonProperty("subindustry")
            private String subIndustry;
        }
    }


}
