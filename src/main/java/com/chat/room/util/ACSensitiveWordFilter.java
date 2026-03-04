package com.chat.room.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class ACSensitiveWordFilter {

    private ACNode root;
    private int wordCount;
    private boolean isBuilt;

    private static class ACNode {
        Map<Character, ACNode> children;
        ACNode fail;
        List<String> outputs;
        boolean isEnd;

        ACNode() {
            this.children = new HashMap<>();
            this.fail = null;
            this.outputs = new ArrayList<>();
            this.isEnd = false;
        }
    }

    public ACSensitiveWordFilter() {
        this.root = new ACNode();
        this.wordCount = 0;
        this.isBuilt = false;
    }

    public void init(Set<String> words) {
        this.root = new ACNode();
        this.wordCount = 0;
        this.isBuilt = false;
        
        for (String word : words) {
            if (word != null && !word.trim().isEmpty()) {
                insert(word.trim());
            }
        }
        
        buildFailureLinks();
        isBuilt = true;
        
        log.info("AC自动机敏感词过滤器初始化完成，共加载 {} 个敏感词", wordCount);
    }

    private void insert(String word) {
        ACNode node = root;
        
        for (int i = 0; i < word.length(); i++) {
            char c = Character.toLowerCase(word.charAt(i));
            node.children.putIfAbsent(c, new ACNode());
            node = node.children.get(c);
        }
        
        if (!node.isEnd) {
            node.isEnd = true;
            node.outputs.add(word);
            wordCount++;
        }
    }

    private void buildFailureLinks() {
        Queue<ACNode> queue = new LinkedList<>();
        
        root.fail = root;
        
        for (ACNode child : root.children.values()) {
            child.fail = root;
            queue.offer(child);
        }
        
        while (!queue.isEmpty()) {
            ACNode current = queue.poll();
            
            for (Map.Entry<Character, ACNode> entry : current.children.entrySet()) {
                char c = entry.getKey();
                ACNode child = entry.getValue();
                
                ACNode failNode = current.fail;
                while (failNode != root && !failNode.children.containsKey(c)) {
                    failNode = failNode.fail;
                }
                
                if (failNode.children.containsKey(c) && failNode.children.get(c) != child) {
                    child.fail = failNode.children.get(c);
                } else {
                    child.fail = root;
                }
                
                child.outputs.addAll(child.fail.outputs);
                
                queue.offer(child);
            }
        }
    }

    public String filter(String text) {
        return filter(text, "*");
    }

    public String filter(String text, String replacement) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        if (wordCount == 0 || !isBuilt) {
            return text;
        }

        char[] chars = text.toCharArray();
        boolean[] mask = new boolean[chars.length];
        
        ACNode current = root;
        
        for (int i = 0; i < text.length(); i++) {
            char c = Character.toLowerCase(text.charAt(i));
            
            while (current != root && !current.children.containsKey(c)) {
                current = current.fail;
            }
            
            if (current.children.containsKey(c)) {
                current = current.children.get(c);
            }
            
            if (!current.outputs.isEmpty()) {
                for (String word : current.outputs) {
                    int start = i - word.length() + 1;
                    for (int j = start; j <= i && j < mask.length; j++) {
                        if (j >= 0) {
                            mask[j] = true;
                        }
                    }
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

        if (wordCount == 0 || !isBuilt) {
            return false;
        }

        ACNode current = root;
        
        for (int i = 0; i < text.length(); i++) {
            char c = Character.toLowerCase(text.charAt(i));
            
            while (current != root && !current.children.containsKey(c)) {
                current = current.fail;
            }
            
            if (current.children.containsKey(c)) {
                current = current.children.get(c);
            }
            
            if (!current.outputs.isEmpty()) {
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

        if (wordCount == 0 || !isBuilt) {
            return found;
        }

        ACNode current = root;
        
        for (int i = 0; i < text.length(); i++) {
            char c = Character.toLowerCase(text.charAt(i));
            
            while (current != root && !current.children.containsKey(c)) {
                current = current.fail;
            }
            
            if (current.children.containsKey(c)) {
                current = current.children.get(c);
            }
            
            if (!current.outputs.isEmpty()) {
                found.addAll(current.outputs);
            }
        }

        return found;
    }

    public List<MatchResult> findAllMatches(String text) {
        List<MatchResult> results = new ArrayList<>();
        
        if (text == null || text.isEmpty() || wordCount == 0 || !isBuilt) {
            return results;
        }

        ACNode current = root;
        
        for (int i = 0; i < text.length(); i++) {
            char c = Character.toLowerCase(text.charAt(i));
            
            while (current != root && !current.children.containsKey(c)) {
                current = current.fail;
            }
            
            if (current.children.containsKey(c)) {
                current = current.children.get(c);
            }
            
            for (String word : current.outputs) {
                int start = i - word.length() + 1;
                results.add(new MatchResult(word, start, i + 1));
            }
        }

        return results;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void clear() {
        root = new ACNode();
        wordCount = 0;
        isBuilt = false;
    }

    public static class MatchResult {
        private final String word;
        private final int startIndex;
        private final int endIndex;

        public MatchResult(String word, int startIndex, int endIndex) {
            this.word = word;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public String getWord() {
            return word;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        @Override
        public String toString() {
            return String.format("MatchResult{word='%s', start=%d, end=%d}", word, startIndex, endIndex);
        }
    }
}
