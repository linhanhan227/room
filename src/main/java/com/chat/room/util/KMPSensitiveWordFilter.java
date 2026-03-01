package com.chat.room.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class KMPSensitiveWordFilter {

    private Set<String> sensitiveWords;
    private Map<String, int[]> failureTables;

    public KMPSensitiveWordFilter() {
        this.sensitiveWords = new HashSet<>();
        this.failureTables = new HashMap<>();
    }

    public void init(Set<String> words) {
        this.sensitiveWords = new HashSet<>();
        this.failureTables = new HashMap<>();
        
        for (String word : words) {
            if (word != null && !word.trim().isEmpty()) {
                String trimmedWord = word.trim();
                sensitiveWords.add(trimmedWord);
                failureTables.put(trimmedWord, buildFailureTable(trimmedWord));
            }
        }
        
        log.info("KMP敏感词过滤器初始化完成，共加载 {} 个敏感词", sensitiveWords.size());
    }

    private int[] buildFailureTable(String pattern) {
        int[] failure = new int[pattern.length()];
        failure[0] = 0;
        
        int j = 0;
        for (int i = 1; i < pattern.length(); i++) {
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = failure[j - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            failure[i] = j;
        }
        
        return failure;
    }

    public String filter(String text) {
        return filter(text, "*");
    }

    public String filter(String text, String replacement) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        if (sensitiveWords.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder(text);
        List<int[]> matches = new ArrayList<>();

        for (String word : sensitiveWords) {
            List<Integer> positions = kmpSearch(text, word);
            for (int pos : positions) {
                matches.add(new int[]{pos, pos + word.length()});
            }
        }

        matches.sort((a, b) -> a[0] - b[0]);

        for (int i = matches.size() - 1; i >= 0; i--) {
            int[] match = matches.get(i);
            for (int j = match[0]; j < match[1]; j++) {
                result.setCharAt(j, replacement.charAt(0));
            }
        }

        return result.toString();
    }

    public boolean contains(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (String word : sensitiveWords) {
            if (kmpSearch(text, word).size() > 0) {
                return true;
            }
        }

        return false;
    }

    public Set<String> getSensitiveWords(String text) {
        Set<String> found = new HashSet<>();
        
        if (text == null || text.isEmpty()) {
            return found;
        }

        for (String word : sensitiveWords) {
            if (kmpSearch(text, word).size() > 0) {
                found.add(word);
            }
        }

        return found;
    }

    private List<Integer> kmpSearch(String text, String pattern) {
        List<Integer> positions = new ArrayList<>();
        
        if (pattern.length() > text.length()) {
            return positions;
        }

        int[] failure = failureTables.get(pattern);
        int j = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = Character.toLowerCase(text.charAt(i));
            char p = Character.toLowerCase(pattern.charAt(j));
            
            while (j > 0 && c != p) {
                j = failure[j - 1];
                p = Character.toLowerCase(pattern.charAt(j));
            }
            
            if (c == p) {
                j++;
            }
            
            if (j == pattern.length()) {
                positions.add(i - pattern.length() + 1);
                j = failure[j - 1];
            }
        }

        return positions;
    }

    public int getWordCount() {
        return sensitiveWords.size();
    }

    public void clear() {
        sensitiveWords.clear();
        failureTables.clear();
    }
}
