package com.centurion.centurion_core.service.impl;

import com.centurion.centurion_core.dto.*;
import com.centurion.centurion_core.entity.AllStocksEntity;
import com.centurion.centurion_core.mapper.StockDataMapper;
import com.centurion.centurion_core.repository.AllStocksRepository;
import com.centurion.centurion_core.service.GenericRestClientService;
import com.centurion.centurion_core.service.StockDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.centurion.centurion_core.constants.CenturionConstants.DATA_SORT_BY;
import static com.centurion.centurion_core.constants.CenturionConstants.DATA_SORT_TYPE;
import static com.centurion.centurion_core.constants.ExternalURI.*;

@Slf4j
@Service
public class StockDataServiceImpl implements StockDataService {

    private final GenericRestClientService genericRestClientService;
    private final AllStocksRepository allStocksRepository;

    public StockDataServiceImpl(GenericRestClientService genericRestClientService, AllStocksRepository allStocksRepository) {
        this.genericRestClientService = genericRestClientService;
        this.allStocksRepository = allStocksRepository;
    }

    private static final Integer PAGE_SIZE = 500;
    private static final Integer TOTAL_PAGE = 9;
    private static List<String> project = List.of("subindustry", "mrktCapf", "lastPrice", "apef");

    @Override
    @Transactional
    public void allStocksDataGroww() {
        byte zero = 0;
        StockDataRequest stockDataRequest = new StockDataRequest(zero, PAGE_SIZE, DATA_SORT_BY, DATA_SORT_TYPE);
        for(byte pageNumber = 0; pageNumber < TOTAL_PAGE; pageNumber++) {
            stockDataRequest.setPage(pageNumber);
            ExternalHttpRequest httpRequest = ExternalHttpRequest.builder()
                    .url(ALL_STOCK_DATA_GROW)
                    .body(stockDataRequest)
                    .httpMethod(HttpMethod.POST).build();
            StocksDataResponse stocksDataResponse = genericRestClientService.execute(httpRequest, StocksDataResponse.class);

            List<AllStocksEntity> allStocksEntityList = StockDataMapper.INSTANCE.toAllStocksEntityList(stocksDataResponse.getRecords());
            allStocksRepository.saveAll(allStocksEntityList);
        }
    }

    @Override
    public void allStockDataTickerTape() {

        TickerTapeStockRequest request = TickerTapeStockRequest.builder()
                .project(project).count(PAGE_SIZE).build();

        int count = 6000;

        ExternalHttpRequest httpRequest = ExternalHttpRequest.builder()
                .url(ALL_STOCK_DATA_TICKER_TAPE)
                .httpMethod(HttpMethod.POST).build();

        for (int offset = 0; offset <= count; offset += PAGE_SIZE) {
            request.setOffset(offset);
            httpRequest.setBody(request);
            TickerTapeStockResponse stockResponse = genericRestClientService.execute(httpRequest, TickerTapeStockResponse.class);
            count = stockResponse.getData().getStats().getCount();
        }
    }
}
