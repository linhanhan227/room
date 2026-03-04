package com.chat.room.service;

import com.chat.room.config.AppProperties;
import com.chat.room.util.ACSensitiveWordFilter;
import com.chat.room.util.KMPSensitiveWordFilter;
import com.chat.room.util.TrieSensitiveWordFilter;
import com.chat.room.util.SensitiveWordLoader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordService {

    private final SensitiveWordLoader wordLoader;
    private final AppProperties appProperties;
    
    private final KMPSensitiveWordFilter kmpFilter;
    private final TrieSensitiveWordFilter trieFilter;
    private final ACSensitiveWordFilter acFilter;

    @PostConstruct
    public void init() {
        reloadSensitiveWords();
    }

    @Scheduled(fixedRateString = "${app.sensitive-word.reload-interval:300000}")
    public void scheduledReload() {
        if (appProperties.getSensitiveWord().isAutoReload()) {
            log.debug("执行定时重新加载敏感词...");
            reloadSensitiveWords();
        }
    }

    public void reloadSensitiveWords() {
        try {
            Set<String> words = wordLoader.loadWords();
            kmpFilter.init(words);
            trieFilter.init(words);
            acFilter.init(words);
            log.info("敏感词库重新加载完成，共 {} 个敏感词，默认算法: {}", words.size(), appProperties.getSensitiveWord().getAlgorithm());
        } catch (Exception e) {
            log.error("重新加载敏感词失败", e);
        }
    }

    public String filterText(String text) {
        if (!appProperties.getSensitiveWord().isEnabled()) {
            return text;
        }
        return filterText(text, getDefaultFilter());
    }

    public String filterText(String text, String algorithm) {
        if (!appProperties.getSensitiveWord().isEnabled()) {
            return text;
        }
        return filterText(text, getFilterByAlgorithm(algorithm));
    }

    private String filterText(String text, Object filter) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        if (filter instanceof KMPSensitiveWordFilter) {
            return kmpFilter.filter(text);
        } else if (filter instanceof TrieSensitiveWordFilter) {
            return trieFilter.filter(text);
        } else if (filter instanceof ACSensitiveWordFilter) {
            return acFilter.filter(text);
        }
        
        return text;
    }

    public boolean containsSensitiveWord(String text) {
        if (!appProperties.getSensitiveWord().isEnabled()) {
            return false;
        }
        return containsSensitiveWord(text, getDefaultFilter());
    }

    public boolean containsSensitiveWord(String text, String algorithm) {
        if (!appProperties.getSensitiveWord().isEnabled()) {
            return false;
        }
        return containsSensitiveWord(text, getFilterByAlgorithm(algorithm));
    }

    private boolean containsSensitiveWord(String text, Object filter) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        if (filter instanceof KMPSensitiveWordFilter) {
            return kmpFilter.contains(text);
        } else if (filter instanceof TrieSensitiveWordFilter) {
            return trieFilter.contains(text);
        } else if (filter instanceof ACSensitiveWordFilter) {
            return acFilter.contains(text);
        }
        
        return false;
    }

    public Set<String> findSensitiveWords(String text) {
        return findSensitiveWords(text, getDefaultFilter());
    }

    public Set<String> findSensitiveWords(String text, String algorithm) {
        return findSensitiveWords(text, getFilterByAlgorithm(algorithm));
    }

    private Set<String> findSensitiveWords(String text, Object filter) {
        if (text == null || text.isEmpty()) {
            return Set.of();
        }
        
        if (filter instanceof KMPSensitiveWordFilter) {
            return kmpFilter.getSensitiveWords(text);
        } else if (filter instanceof TrieSensitiveWordFilter) {
            return trieFilter.getSensitiveWords(text);
        } else if (filter instanceof ACSensitiveWordFilter) {
            return acFilter.getSensitiveWords(text);
        }
        
        return Set.of();
    }

    public List<ACSensitiveWordFilter.MatchResult> findAllMatches(String text) {
        return acFilter.findAllMatches(text);
    }

    public int getSensitiveWordCount() {
        return wordLoader.getWordCount();
    }

    public Set<String> getAllSensitiveWords() {
        return wordLoader.getSensitiveWords();
    }

    public void addSensitiveWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        wordLoader.addWord(word.trim());
        reloadFilters();
        log.info("添加敏感词: {}", word);
    }

    public void removeSensitiveWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        wordLoader.removeWord(word.trim());
        reloadFilters();
        log.info("移除敏感词: {}", word);
    }

    public void saveSensitiveWords() {
        wordLoader.saveToFile();
        log.info("敏感词已保存到文件");
    }

    public String getDefaultAlgorithm() {
        return appProperties.getSensitiveWord().getAlgorithm();
    }

    public void setDefaultAlgorithm(String algorithm) {
        appProperties.getSensitiveWord().setAlgorithm(algorithm.toUpperCase());
        log.info("默认敏感词过滤算法已设置为: {}", appProperties.getSensitiveWord().getAlgorithm());
    }

    private void reloadFilters() {
        Set<String> words = wordLoader.getSensitiveWords();
        kmpFilter.init(words);
        trieFilter.init(words);
        acFilter.init(words);
    }

    private Object getDefaultFilter() {
        return getFilterByAlgorithm(appProperties.getSensitiveWord().getAlgorithm());
    }

    private Object getFilterByAlgorithm(String algorithm) {
        if (algorithm == null) {
            return acFilter;
        }
        
        switch (algorithm.toUpperCase()) {
            case "KMP":
                return kmpFilter;
            case "TRIE":
                return trieFilter;
            case "AC":
            default:
                return acFilter;
        }
    }
}
