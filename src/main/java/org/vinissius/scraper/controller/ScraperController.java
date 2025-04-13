package org.vinissius.scraper.controller;

import org.springframework.web.bind.annotation.*;
import org.vinissius.scraper.entity.ProductEntity;
import org.vinissius.scraper.service.ScraperService;

import java.util.List;

@RestController
@RequestMapping("/api/scraper")
public class ScraperController {

    private final ScraperService scraperService;

    public ScraperController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    /**
     * Ex: GET /api/scraper?max=5
     */
    @GetMapping
    public List<ProductEntity> scrape(@RequestParam(name = "max", defaultValue = "10") int max) {
        return scraperService.scrapeAll(max);
    }
}
