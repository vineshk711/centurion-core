package com.centurion.centurion_core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class StockDataRequest implements Serializable {
    private ListFilters listFilters;
    private ObjFilters objFilters;
    private Byte page;
    private Integer size;
    private String sortBy;
    private String sortType;

    public StockDataRequest(Byte page, Integer size, String sortBy, String sortType) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortType = sortType;

        this.listFilters = new ListFilters();
        this.objFilters = new ObjFilters();
    }

    @Getter
    @Setter
    public class ListFilters {
        @JsonProperty("INDUSTRY")
        private String[] industry;
        @JsonProperty("INDEX")
        private String[] index;

        public ListFilters() {
            this.industry = new String[0];
            this.index = new String[0];
        }

    }

    @Getter
    @Setter
    public class ObjFilters {
        @JsonProperty("CLOSE_PRICE")
        private PriceFilter closePrice;
        @JsonProperty("MARKET_CAP")
        private PriceFilter marketCap;

        public ObjFilters() {
            this.closePrice = new PriceFilter();
            this.marketCap = new PriceFilter();
        }
    }

    @Setter
    @Getter
    public class PriceFilter {
        private Double max;
        private Double min;
    }
}
