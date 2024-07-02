package com.github.natche.cyderutils.network.scrapers;

/** An interface all results returned by {@link Scraper}s must implement. */
public interface ScraperResult {
    /**
     * Returns the URL these results were scraped from.
     *
     * @return the URL these results were scraped from
     */
    String getFromUrl();
}
