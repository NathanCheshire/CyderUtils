package com.github.natche.cyderutils.network.scrapers;

import java.util.Optional;

/** An interface all scrapers must implement. */
public interface Scraper {
    /**
     * Scrapes using this scraper and returns a new {@link ScraperResult}.
     *
     * @return a new {@link ScraperResult}
     */
    static Optional<? extends ScraperResult> scape() {
        return Optional.empty();
    }
}
