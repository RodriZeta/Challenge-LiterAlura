package com.gutendex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GutendexBook {
    private String title;
    private List<String> languages;

    @JsonProperty("download_count")
    private Integer downloadCount;

    private List<GutendexAuthor> authors;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public List<GutendexAuthor> getAuthors() {
        return authors;
    }

    public void setAuthors(List<GutendexAuthor> authors) {
        this.authors = authors;
    }
}
