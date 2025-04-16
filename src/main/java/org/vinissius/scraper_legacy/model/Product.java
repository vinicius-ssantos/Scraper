package org.vinissius.scraper_legacy.model;

import java.util.List;

public class Product {
    private String title;
    private String price;
    private String rating;
    private String reviewCount;
    private List<String> bulletPoints;
    private String productDescription;
    private List<String> images;
    private String sellerInfo;
    private String asin;
    private String executedAt;
    private String url;

    // Construtor completo (poderia criar um Builder, mas aqui faremos getters/setters)
    public Product() {}

    // Getters e Setters

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getReviewCount() { return reviewCount; }
    public void setReviewCount(String reviewCount) { this.reviewCount = reviewCount; }

    public List<String> getBulletPoints() { return bulletPoints; }
    public void setBulletPoints(List<String> bulletPoints) { this.bulletPoints = bulletPoints; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getSellerInfo() { return sellerInfo; }
    public void setSellerInfo(String sellerInfo) { this.sellerInfo = sellerInfo; }

    public String getAsin() { return asin; }
    public void setAsin(String asin) { this.asin = asin; }

    public String getExecutedAt() { return executedAt; }
    public void setExecutedAt(String executedAt) { this.executedAt = executedAt; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    @Override
    public String toString() {
        return "Product{" +
                "title='" + title + '\'' +
                ", price='" + price + '\'' +
                ", rating='" + rating + '\'' +
                ", reviewCount='" + reviewCount + '\'' +
                ", bulletPoints=" + bulletPoints +
                ", productDescription='" + productDescription + '\'' +
                ", images=" + images +
                ", sellerInfo='" + sellerInfo + '\'' +
                ", asin='" + asin + '\'' +
                ", executedAt='" + executedAt + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
