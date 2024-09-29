package com.centurion.centurion_core.controller;

import com.centurion.centurion_core.service.StockDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.centurion.centurion_core.constants.RootURI.STOCK_DATA;
import static com.centurion.centurion_core.constants.StockDataURI.ALL;
import static com.centurion.centurion_core.constants.StockDataURI.ALL_TICKER_TAPE;

@RestController
@RequestMapping(STOCK_DATA)
public class StockDataController {

    private final StockDataService dataFetchService;

    public StockDataController(StockDataService dataFetchService) {
        this.dataFetchService = dataFetchService;
    }

    @PostMapping(ALL)
    public ResponseEntity<Void> saveAllStocksMetaData() {
        dataFetchService.allStocksDataGroww();
        return ResponseEntity.ok().build();
    }


    @PostMapping(ALL_TICKER_TAPE)
    public ResponseEntity<Void> allStockDataTickerTape() {
        dataFetchService.allStockDataTickerTape();
        return ResponseEntity.ok().build();
    }

}
