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

    @Column(name = "company_short_name")
    private String companyShortName;

    @Column(name = "industry_code")
    private Short industryCode;

    @Column(name = "bse_script_code")
    private Integer bseScriptCode;

    @Column(name = "nse_script_code")
    private String nseScriptCode;

    @Column(name = "market_cap")
    private Long marketCap;
}
