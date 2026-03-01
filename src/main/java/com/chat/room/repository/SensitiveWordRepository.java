package com.chat.room.repository;

import com.chat.room.entity.SensitiveWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {

    Optional<SensitiveWord> findByWord(String word);

    boolean existsByWord(String word);

    List<SensitiveWord> findByEnabledTrue();

    Page<SensitiveWord> findByCategory(String category, Pageable pageable);

    @Query("SELECT sw FROM SensitiveWord sw WHERE sw.enabled = true AND sw.level >= :level")
    List<SensitiveWord> findActiveWordsByLevel(@Param("level") Integer level);

    @Query("SELECT sw FROM SensitiveWord sw WHERE sw.word LIKE %:keyword%")
    Page<SensitiveWord> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT sw.category FROM SensitiveWord sw WHERE sw.category IS NOT NULL")
    List<String> findAllCategories();

    @Query("SELECT sw.word FROM SensitiveWord sw WHERE sw.enabled = true")
    List<String> findAllActiveWords();
}
