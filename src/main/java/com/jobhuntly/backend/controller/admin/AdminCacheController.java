package com.jobhuntly.backend.controller.admin;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.jobhuntly.backend.constant.CacheConstant.AI_MATCH;
import static com.jobhuntly.backend.constant.CacheConstant.AI_MATCH_BYPASS;

@RestController
@RequestMapping("${backend.prefix}/admin/cache")
public class AdminCacheController {

    private final CacheManager cacheManager;

    public AdminCacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private ResponseEntity<?> okNoStore(Object body) {
        HttpHeaders h = new HttpHeaders();
        h.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        h.add("Pragma", "no-cache");
        return ResponseEntity.ok().headers(h).body(body);
    }

    // Xóa theo tên cache. Nếu không có entryKey => clear toàn bộ cache
    @DeleteMapping
    public ResponseEntity<?> deleteByCacheName(
            @RequestParam String cacheName,
            @RequestParam(required = false) String entryKey
    ) {
        Cache c = cacheManager.getCache(cacheName);
        if (c == null) return ResponseEntity.notFound().build();
        Map<String, Object> res = new HashMap<>();
        res.put("cacheName", cacheName);
        if (entryKey == null || entryKey.isBlank()) {
            c.clear();
            res.put("cleared", true);
            // nếu clear toàn bộ AI_MATCH -> bypass 1 lần cho tất cả key
            if (AI_MATCH.equals(cacheName)) {
                Cache bypass = cacheManager.getCache(AI_MATCH_BYPASS);
                if (bypass != null) bypass.put("__ALL__", Boolean.TRUE);
                res.put("bypassOnceAll", true);
            }
            return okNoStore(res);
        } else {
            boolean existed = c.get(entryKey) != null;
            c.evict(entryKey);
            boolean deleted = c.get(entryKey) == null;
            res.put("entryKey", entryKey);
            res.put("existed", existed);
            res.put("deleted", deleted);
            // nếu là AI_MATCH thì set marker bypass một lần
            if (AI_MATCH.equals(cacheName)) {
                Cache bypass = cacheManager.getCache(AI_MATCH_BYPASS);
                if (bypass != null) bypass.put(entryKey, Boolean.TRUE);
                res.put("bypassOnce", true);
            }
            return okNoStore(res);
        }
    }

    // Giữ tương thích: xóa cache ai:match theo key hoặc theo bộ userId+jobId+resumeHash
    @DeleteMapping("/ai-match")
    public ResponseEntity<?> deleteAiMatch(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String resumeHash
    ) {
        Cache cache = cacheManager.getCache(AI_MATCH);
        if (cache == null) return ResponseEntity.ok().build();
        String k = key;
        if (k == null || k.isBlank()) {
            if (userId == null || jobId == null || resumeHash == null || resumeHash.isBlank()) {
                Map<String, String> err = Map.of("error", "Missing key or (userId, jobId, resumeHash)");
                return ResponseEntity.badRequest().body(err);
            }
            k = userId + ":" + jobId + ":" + resumeHash;
        }
        boolean existed = cache.get(k) != null;
        cache.evict(k);
        boolean deleted = cache.get(k) == null;
        // set marker bypass 1 lần
        Cache bypass = cacheManager.getCache(AI_MATCH_BYPASS);
        if (bypass != null) bypass.put(k, Boolean.TRUE);

        Map<String, Object> res = new HashMap<>();
        res.put("cacheName", AI_MATCH);
        res.put("entryKey", k);
        res.put("existed", existed);
        res.put("deleted", deleted);
        res.put("bypassOnce", true);
        return okNoStore(res);
    }
} 