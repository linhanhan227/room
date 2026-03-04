package com.chat.room.controller;

import com.chat.room.dto.ApiResponse;
import com.chat.room.service.SensitiveWordService;
import com.chat.room.util.ACSensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/admin/sensitive-words")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addWord(@RequestBody String word) {
        sensitiveWordService.addSensitiveWord(word);
        return ResponseEntity.ok(ApiResponse.success("敏感词添加成功", null));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchAddWords(@RequestBody List<String> words) {
        for (String word : words) {
            sensitiveWordService.addSensitiveWord(word);
        }
        return ResponseEntity.ok(ApiResponse.success("批量添加敏感词成功", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteWord(@RequestBody String word) {
        sensitiveWordService.removeSensitiveWord(word);
        return ResponseEntity.ok(ApiResponse.success("敏感词删除成功", null));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchDeleteWords(@RequestBody List<String> words) {
        for (String word : words) {
            sensitiveWordService.removeSensitiveWord(word);
        }
        return ResponseEntity.ok(ApiResponse.success("批量删除敏感词成功", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Set<String>>> getAllWords() {
        Set<String> words = sensitiveWordService.getAllSensitiveWords();
        return ResponseEntity.ok(ApiResponse.success(words));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getWordCount() {
        int count = sensitiveWordService.getSensitiveWordCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping("/reload")
    public ResponseEntity<ApiResponse<Void>> reload() {
        sensitiveWordService.reloadSensitiveWords();
        return ResponseEntity.ok(ApiResponse.success("敏感词库重新加载成功", null));
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Void>> save() {
        sensitiveWordService.saveSensitiveWords();
        return ResponseEntity.ok(ApiResponse.success("敏感词已保存到文件", null));
    }

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkText(
            @RequestBody String text,
            @RequestParam(required = false) String algorithm) {
        boolean contains = algorithm != null 
                ? sensitiveWordService.containsSensitiveWord(text, algorithm)
                : sensitiveWordService.containsSensitiveWord(text);
        return ResponseEntity.ok(ApiResponse.success(contains));
    }

    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<String>> filterText(
            @RequestBody String text,
            @RequestParam(required = false) String algorithm) {
        String filtered = algorithm != null
                ? sensitiveWordService.filterText(text, algorithm)
                : sensitiveWordService.filterText(text);
        return ResponseEntity.ok(ApiResponse.success("过滤完成", filtered));
    }

    @PostMapping("/find")
    public ResponseEntity<ApiResponse<Set<String>>> findSensitiveWords(
            @RequestBody String text,
            @RequestParam(required = false) String algorithm) {
        Set<String> found = algorithm != null
                ? sensitiveWordService.findSensitiveWords(text, algorithm)
                : sensitiveWordService.findSensitiveWords(text);
        return ResponseEntity.ok(ApiResponse.success(found));
    }

    @PostMapping("/matches")
    public ResponseEntity<ApiResponse<List<ACSensitiveWordFilter.MatchResult>>> findAllMatches(@RequestBody String text) {
        List<ACSensitiveWordFilter.MatchResult> matches = sensitiveWordService.findAllMatches(text);
        return ResponseEntity.ok(ApiResponse.success(matches));
    }

    @GetMapping("/algorithm")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAlgorithm() {
        Map<String, Object> result = new HashMap<>();
        result.put("defaultAlgorithm", sensitiveWordService.getDefaultAlgorithm());
        result.put("availableAlgorithms", List.of("KMP", "TRIE", "AC"));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/algorithm")
    public ResponseEntity<ApiResponse<Void>> setAlgorithm(@RequestParam String algorithm) {
        sensitiveWordService.setDefaultAlgorithm(algorithm);
        return ResponseEntity.ok(ApiResponse.success("默认算法设置成功", null));
    }
}
