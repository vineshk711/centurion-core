package com.centurion.centurion_core.controller;

import com.centurion.centurion_core.service.StockDataService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.centurion.centurion_core.constants.RootURI.STOCK_DATA;
import static com.centurion.centurion_core.constants.StockDataURI.ALL;

@RestController
@RequestMapping(STOCK_DATA)
public class StockDataController {

    private final StockDataService dataFetchService;

    public StockDataController(StockDataService dataFetchService) {
        this.dataFetchService = dataFetchService;
    }

    @PostMapping(ALL)
    @ResponseStatus(code = HttpStatus.OK)
    public void saveAllStocksMetaData() {
        dataFetchService.fetchAllStocksMetaData();
    }
}
