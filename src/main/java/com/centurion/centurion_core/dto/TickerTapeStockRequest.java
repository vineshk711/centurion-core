package com.centurion.centurion_core.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class TickerTapeStockRequest implements Serializable {
    private List<String> project;
    private Integer offset;
    private Integer count;
}
