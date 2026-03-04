package com.chat.room.util;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class DFASensitiveWordFilter {

    private Map<Character, Object> sensitiveWordMap;
    private static final String END_FLAG = "isEnd";
    private static final String REPLACEMENT = "*";

    public DFASensitiveWordFilter() {
        this.sensitiveWordMap = new HashMap<>();
    }

    public void init(Set<String> sensitiveWords) {
        this.sensitiveWordMap = new HashMap<>();
        for (String word : sensitiveWords) {
            if (word != null && !word.trim().isEmpty()) {
                addWordToMap(word.trim());
            }
        }
        log.info("DFA敏感词树初始化完成，共加载 {} 个敏感词", sensitiveWords.size());
    }

    @SuppressWarnings("unchecked")
    private void addWordToMap(String word) {
        Map<Character, Object> currentMap = sensitiveWordMap;
        
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Object obj = currentMap.get(c);
            
            if (obj == null) {
                Map<Character, Object> newMap = new HashMap<>();
                newMap.put(END_FLAG.charAt(0), false);
                currentMap.put(c, newMap);
                currentMap = newMap;
            } else {
                currentMap = (Map<Character, Object>) obj;
            }
            
            if (i == word.length() - 1) {
                currentMap.put(END_FLAG.charAt(0), true);
            }
        }
    }

    public String filter(String text) {
        return filter(text, REPLACEMENT);
    }

    public String filter(String text, String replacement) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        if (sensitiveWordMap == null || sensitiveWordMap.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder(text);
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            int matchLength = checkSensitiveWord(text, i);
            if (matchLength > 0) {
                for (int j = i; j < i + matchLength; j++) {
                    result.setCharAt(j, replacement.charAt(0));
                }
                i = i + matchLength - 1;
            }
        }

        return result.toString();
    }

    public boolean contains(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        if (sensitiveWordMap == null || sensitiveWordMap.isEmpty()) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            if (checkSensitiveWord(text, i) > 0) {
                return true;
            }
        }

        return false;
    }

    public Set<String> getSensitiveWords(String text) {
        Set<String> sensitiveWords = new HashSet<>();
        
        if (text == null || text.isEmpty()) {
            return sensitiveWords;
        }

        if (sensitiveWordMap == null || sensitiveWordMap.isEmpty()) {
            return sensitiveWords;
        }

        for (int i = 0; i < text.length(); i++) {
            int matchLength = checkSensitiveWord(text, i);
            if (matchLength > 0) {
                sensitiveWords.add(text.substring(i, i + matchLength));
                i = i + matchLength - 1;
            }
        }

        return sensitiveWords;
    }

    @SuppressWarnings("unchecked")
    private int checkSensitiveWord(String text, int beginIndex) {
        Map<Character, Object> currentMap = sensitiveWordMap;
        int matchLength = 0;
        int length = text.length();
        boolean isEnd = false;

        for (int i = beginIndex; i < length; i++) {
            char c = Character.toLowerCase(text.charAt(i));
            currentMap = (Map<Character, Object>) currentMap.get(c);
            
            if (currentMap == null) {
                break;
            }
            
            matchLength++;
            
            if (Boolean.TRUE.equals(currentMap.get(END_FLAG.charAt(0)))) {
                isEnd = true;
                break;
            }
        }

        if (!isEnd) {
            matchLength = 0;
        }

        return matchLength;
    }

    public int getWordCount() {
        return countWords(sensitiveWordMap);
    }

    @SuppressWarnings("unchecked")
    private int countWords(Map<Character, Object> map) {
        int count = 0;
        for (Map.Entry<Character, Object> entry : map.entrySet()) {
            if (entry.getKey().equals(END_FLAG.charAt(0))) {
                if (Boolean.TRUE.equals(entry.getValue())) {
                    count++;
                }
            } else {
                count += countWords((Map<Character, Object>) entry.getValue());
            }
        }
        return count;
    }

    public void clear() {
        if (sensitiveWordMap != null) {
            sensitiveWordMap.clear();
        }
    }
}
