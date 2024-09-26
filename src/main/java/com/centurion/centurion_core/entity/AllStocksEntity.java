package com.centurion.centurion_core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "all_stocks")
public class AllStocksEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "company_name", unique = true)
    private String companyName;

    @Column(name = "market_cap")
    private Long marketCap;

    @Column(name = "stock_id")
    private String stockId;

    @Column(name = "sector")
    private String sector;
}
