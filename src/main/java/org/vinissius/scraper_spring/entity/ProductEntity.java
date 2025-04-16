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
    private String bulletPoints; // Armazena JSON

    @Column(length = 2000)
    private String productDescription;

    @Column(length = 2000)
    private String images; // Armazena JSON

    private String sellerInfo;
    private String executedAt;

    // Alterado para permitir URLs maiores (por exemplo, 2048 caracteres)
    @Column(length = 2048)
    private String url;
}
