package com.centurion.centurion_core.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockDataResponseDTO {
    private List<StockDataResponse> results;

    @Data
    public static class StockDataResponse {
        private String sid;
        private Stock stock;

        @Data
        public static class Stock {
            private StockInfo info;
            private Ratio advancedRatios;
        }

        @Data
        public static class StockInfo {
            private String name;
            private String sector;
        }

        @Data
        public static class Ratio {
            private Double apef;
            private Double mrktCapf;
            private Double lastPrice;
        }
    }

}