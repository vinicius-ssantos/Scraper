package org.vinissius.scraper_spring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String asin;
    private String title;
    private String price;
    private String rating;
    private String reviewCount;
    @Column(length = 2000)
    private String bulletPoints; // JSON
    @Column(length = 2000)
    private String productDescription;
    @Column(length = 2000)
    private String images; // JSON
    private String sellerInfo;
    private String executedAt;
    private String url;

    // getters e setters
    // ...
}
