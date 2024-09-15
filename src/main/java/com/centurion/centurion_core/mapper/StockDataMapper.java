package com.centurion.centurion_core.mapper;

import com.centurion.centurion_core.dto.StocksDataResponse.StocksData;
import com.centurion.centurion_core.entity.AllStocksEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface StockDataMapper {

    StockDataMapper INSTANCE = Mappers.getMapper(StockDataMapper.class);

    List<AllStocksEntity> toAllStocksEntityList(List<StocksData> stocksDataList);
}
