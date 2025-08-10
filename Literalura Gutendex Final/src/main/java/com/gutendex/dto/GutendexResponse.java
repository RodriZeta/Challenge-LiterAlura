package com.gutendex.dto;

import java.util.List;

public class GutendexResponse {
    private List<BookDTO> results;

    public List<BookDTO> getResults() { return results; }
    public void setResults(List<BookDTO> results) { this.results = results; }
}
