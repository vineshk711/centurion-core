package com.centurion.centurion_core.repository;

import com.centurion.centurion_core.entity.AllStocksEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllStocksRepository extends JpaRepository<AllStocksEntity, Long> {
}
