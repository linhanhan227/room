package com.chat.room.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TrieSensitiveWordFilter {

    private TrieNode root;
    private int wordCount;

    private static class TrieNode {
        Map<Character, TrieNode> children;
        boolean isEnd;
        String word;

        TrieNode() {
            this.children = new HashMap<>();
            this.isEnd = false;
            this.word = null;
        }
    }

    public TrieSensitiveWordFilter() {
        this.root = new TrieNode();
        this.wordCount = 0;
    }

    public void init(Set<String> words) {
        this.root = new TrieNode();
        this.wordCount = 0;
        
        for (String word : words) {
            if (word != null && !word.trim().isEmpty()) {
                insert(word.trim());
            }
        }
        
        log.info("Trie树敏感词过滤器初始化完成，共加载 {} 个敏感词", wordCount);
    }

    private void insert(String word) {
        TrieNode node = root;
        
        for (int i = 0; i < word.length(); i++) {
            char c = Character.toLowerCase(word.charAt(i));
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        
        if (!node.isEnd) {
            node.isEnd = true;
            node.word = word;
            wordCount++;
        }
    }

    public String filter(String text) {
        return filter(text, "*");
    }

    public String filter(String text, String replacement) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        if (wordCount == 0) {
            return text;
        }

        char[] chars = text.toCharArray();
        boolean[] mask = new boolean[chars.length];

        for (int i = 0; i < text.length(); i++) {
            int matchLength = searchFromPosition(text, i);
            if (matchLength > 0) {
                for (int j = i; j < i + matchLength && j < mask.length; j++) {
                    mask[j] = true;
                }
            }
        }

        for (int i = 0; i < chars.length; i++) {
            if (mask[i]) {
                chars[i] = replacement.charAt(0);
            }
        }

        return new String(chars);
    }

    public boolean contains(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            if (searchFromPosition(text, i) > 0) {
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

        for (int i = 0; i < text.length(); i++) {
            String word = findWordFromPosition(text, i);
            if (word != null) {
                found.add(word);
            }
        }

        return found;
    }

    private int searchFromPosition(String text, int start) {
        TrieNode node = root;
        int matchLength = 0;
        int lastMatchLength = 0;

        for (int i = start; i < text.length(); i++) {
            char c = Character.toLowerCase(text.charAt(i));
            
            if (!node.children.containsKey(c)) {
                break;
            }
            
            node = node.children.get(c);
            matchLength++;
            
            if (node.isEnd) {
                lastMatchLength = matchLength;
            }
        }

        return lastMatchLength;
    }

    private String findWordFromPosition(String text, int start) {
        TrieNode node = root;
        String lastWord = null;

        for (int i = start; i < text.length(); i++) {
            char c = Character.toLowerCase(text.charAt(i));
            
            if (!node.children.containsKey(c)) {
                break;
            }
            
            node = node.children.get(c);
            
            if (node.isEnd) {
                lastWord = node.word;
            }
        }

        return lastWord;
    }

    public boolean search(String word) {
        TrieNode node = root;
        
        for (int i = 0; i < word.length(); i++) {
            char c = Character.toLowerCase(word.charAt(i));
            if (!node.children.containsKey(c)) {
                return false;
            }
            node = node.children.get(c);
        }
        
        return node.isEnd;
    }

    public boolean startsWith(String prefix) {
        TrieNode node = root;
        
        for (int i = 0; i < prefix.length(); i++) {
            char c = Character.toLowerCase(prefix.charAt(i));
            if (!node.children.containsKey(c)) {
                return false;
            }
            node = node.children.get(c);
        }
        
        return true;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void clear() {
        root = new TrieNode();
        wordCount = 0;
    }
}
