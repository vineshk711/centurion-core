package com.centurion.centurion_core.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StocksDataResponse {
    private List<StocksData> records;
    private Integer totalRecords;

    @Getter
    @Setter
    public static class StocksData {
        private String isin;
        private String growwContractId;
        private String companyName;
        private String companyShortName;
        private String searchId;
        private Short industryCode;
        private Integer bseScriptCode;
        private String nseScriptCode;
        private Double yearlyHighPrice;
        private Double yearlyLowPrice;
        private Double closePrice;
        private Long marketCap;
        private LivePriceDto livePriceDto;
    }

    @Getter
    @Setter
    private static class LivePriceDto {
        private String type;
        private String symbol;
        private Long tsInMillis;
        private Double open;
        private Double high;
        private Double low;
        private Double close;
        private Double ltp;
        private Double dayChange;
        private Double dayChangePerc;
        private Double lowPriceRange;
        private Double highPriceRange;
        private Integer volume;
        private Double totalBuyQty;
        private Double totalSellQty;
        private Double oiDayChange;
        private Double oiDayChangePerc;
        private Integer lastTradeQty;
        private Long lastTradeTime;
    }
}
