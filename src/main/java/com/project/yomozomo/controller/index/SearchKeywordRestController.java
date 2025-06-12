package com.project.yomozomo.controller.index;

import com.project.yomozomo.service.SearchService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchKeywordRestController {

    private final SearchService searchService;

    public SearchKeywordRestController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/api/popular-keywords")
    public List<String> getPopularKeywords() {
        return searchService.getPopularKeywords();
    }

    @GetMapping("/session-keywords")
    public List<String> getSessionKeywords(HttpSession session) {
        List<String> recentKeywords = (List<String>) session.getAttribute("recentKeywords");
        return recentKeywords != null ? recentKeywords : List.of();
    }
}