package com.chat.room.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Component
public class SensitiveWordLoader {

    @Value("${sensitive-word.file-path:sensitive_words.txt}")
    private String filePath;

    @Value("${sensitive-word.enabled:true}")
    private boolean enabled;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile Set<String> sensitiveWords = new HashSet<>();

    public Set<String> loadWords() {
        lock.writeLock().lock();
        try {
            Set<String> words = new HashSet<>();
            
            if (!enabled) {
                log.info("敏感词过滤功能已禁用");
                this.sensitiveWords = words;
                return words;
            }

            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path)) {
                    words = loadFromFile(path);
                    log.info("从外部文件加载敏感词: {}, 共 {} 个", filePath, words.size());
                } else {
                    ClassPathResource resource = new ClassPathResource(filePath);
                    if (resource.exists()) {
                        words = loadFromClasspath(resource);
                        log.info("从classpath加载敏感词: {}, 共 {} 个", filePath, words.size());
                    } else {
                        log.warn("敏感词文件不存在: {}, 使用默认敏感词", filePath);
                        words = loadDefaultWords();
                    }
                }
            } catch (Exception e) {
                log.error("加载敏感词文件失败: {}", e.getMessage());
                words = loadDefaultWords();
            }

            this.sensitiveWords = words;
            return words;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Set<String> getSensitiveWords() {
        lock.readLock().lock();
        try {
            return new HashSet<>(sensitiveWords);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getWordCount() {
        lock.readLock().lock();
        try {
            return sensitiveWords.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        lock.writeLock().lock();
        try {
            Set<String> newWords = new HashSet<>(sensitiveWords);
            newWords.add(word.trim());
            this.sensitiveWords = newWords;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        lock.writeLock().lock();
        try {
            Set<String> newWords = new HashSet<>(sensitiveWords);
            newWords.remove(word.trim());
            this.sensitiveWords = newWords;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void saveToFile() {
        lock.readLock().lock();
        try {
            Path path = Paths.get(filePath);
            Set<String> words = new HashSet<>(sensitiveWords);
            
            try {
                Files.write(path, words, StandardCharsets.UTF_8);
                log.info("敏感词已保存到文件: {}, 共 {} 个", filePath, words.size());
            } catch (IOException e) {
                log.error("保存敏感词文件失败: {}", e.getMessage());
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private Set<String> loadFromFile(Path path) throws IOException {
        Set<String> words = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    words.add(line);
                }
            }
        }
        return words;
    }

    private Set<String> loadFromClasspath(ClassPathResource resource) throws IOException {
        Set<String> words = new HashSet<>();
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    words.add(line);
                }
            }
        }
        return words;
    }

    private Set<String> loadDefaultWords() {
        Set<String> words = new HashSet<>();
        words.add("敏感词");
        words.add("违禁词");
        words.add("测试敏感词");
        return words;
    }

    public void reload() {
        log.info("重新加载敏感词文件...");
        loadWords();
    }
}
