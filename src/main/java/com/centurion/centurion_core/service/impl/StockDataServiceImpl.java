package com.centurion.centurion_core.service.impl;

import com.centurion.centurion_core.dto.ExternalHttpRequest;
import com.centurion.centurion_core.dto.StockDataRequest;
import com.centurion.centurion_core.dto.StocksDataResponse;
import com.centurion.centurion_core.entity.AllStocksEntity;
import com.centurion.centurion_core.mapper.StockDataMapper;
import com.centurion.centurion_core.repository.AllStocksRepository;
import com.centurion.centurion_core.service.GenericRestClientService;
import com.centurion.centurion_core.service.StockDataService;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
