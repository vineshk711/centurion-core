package com.centurion.centurion_core.service.impl;

import com.centurion.centurion_core.dto.*;
import com.centurion.centurion_core.entity.AllStocksEntity;
import com.centurion.centurion_core.mapper.StockDataMapper;
import com.centurion.centurion_core.repository.AllStocksRepository;
import com.centurion.centurion_core.service.GenericRestClientService;
import com.centurion.centurion_core.service.StockDataService;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

import static com.centurion.centurion_core.constants.CenturionConstants.DATA_SORT_BY;
import static com.centurion.centurion_core.constants.CenturionConstants.DATA_SORT_TYPE;
import static com.centurion.centurion_core.constants.ExternalURI.*;

@Service
public class StockDataServiceImpl implements StockDataService {

    private final GenericRestClientService genericRestClientService;
    private final AllStocksRepository allStocksRepository;

    public StockDataServiceImpl(GenericRestClientService genericRestClientService, AllStocksRepository allStocksRepository) {
        this.genericRestClientService = genericRestClientService;
        this.allStocksRepository = allStocksRepository;
    }

    public static final Integer PAGE_SIZE = 500;
    public static final Integer TOTAL_PAGE = 9;
    public static final String MARKET_CAP = "mrktCapf";

    @Override
    @Transactional
    public void fetchAllStocksMetaData() {
        byte zero = 0;
        StockDataRequest stockDataRequest = new StockDataRequest(zero, PAGE_SIZE, DATA_SORT_BY, DATA_SORT_TYPE);
        for(byte pageNumber = 0; pageNumber < TOTAL_PAGE; pageNumber++) {
            stockDataRequest.setPage(pageNumber);
            ExternalHttpRequest httpRequest = ExternalHttpRequest.builder()
                    .url(ALL_STOCK_DATA)
                    .body(stockDataRequest)
                    .httpMethod(HttpMethod.POST).build();
            StocksDataResponse stocksDataResponse = genericRestClientService.execute(httpRequest, StocksDataResponse.class);

            List<AllStocksEntity> allStocksEntityList = StockDataMapper.INSTANCE.toAllStocksEntityList(stocksDataResponse.getRecords());
            allStocksRepository.saveAll(allStocksEntityList);
        }
    }

    @Override
    public Object saveStocksMetaData() {
//        TrackerTapeStockDataRequest request = TrackerTapeStockDataRequest.builder()
//                .sortBy(MARKET_CAP)
//                .sortOrder(-1)
//                .project(List.of("apef", "mrktCapf", "lastPrice"))
//                .build();
//        int offset = 0, count = 500, total = Integer.MAX_VALUE;
//        for(int i = offset; i<=total; i++) {
//            request.setOffset(offset);
//            request.setCount(count);
//            ExternalHttpRequest httpRequest = ExternalHttpRequest.builder()
//                    .url(TRACKER_PAGE_ALL_STOCK_DATA)
//                    .body(request)
//                    .httpMethod(HttpMethod.POST).build();
//            TrackerTapeResponse res =  genericRestClientService.execute(httpRequest, TrackerTapeResponse.class);
//
//        }
        return null;

    }
}
