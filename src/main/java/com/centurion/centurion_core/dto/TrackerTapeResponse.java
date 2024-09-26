package com.centurion.centurion_core.dto;

import lombok.Data;

@Data
public class  TrackerTapeResponse {
    private Boolean success;
    private StockDataResponseDTO data;
    private Stats stats;

    @Data
    public static class Stats {
        private Integer count;
    }
}
