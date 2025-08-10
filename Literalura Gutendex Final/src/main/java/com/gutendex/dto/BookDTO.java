package com.gutendex.dto;

import java.util.List;

public class BookDTO {
    private String title;
    private List<String> languages;
    private int download_count;
    private List<AuthorDTO> authors;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }
    public int getDownload_count() { return download_count; }
    public void setDownload_count(int download_count) { this.download_count = download_count; }
    public List<AuthorDTO> getAuthors() { return authors; }
    public void setAuthors(List<AuthorDTO> authors) { this.authors = authors; }
}
