package com.example.urlshortener.repository;

import com.example.urlshortener.entity.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByShortCodeOrCustomAliasIgnoreCase(String shortCode, String customAlias);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAliasIgnoreCase(String customAlias);

    Page<Url> findByUserId(Long userId, Pageable pageable);

    Page<Url> findByUserIdAndActive(Long userId, boolean active, Pageable pageable);

    List<Url> findByUserId(Long userId);

    long countByActiveTrue();

    long countByCreatedAtAfter(Instant createdAt);

    List<Url> findTop10ByOrderByClickCountDesc();

    @Query("""
        SELECT u
        FROM Url u
        WHERE LOWER(u.originalUrl) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(u.shortCode) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(u.customAlias) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Page<Url> findBySearchTerm(
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
        SELECT u
        FROM Url u
        WHERE u.user.id = :userId
          AND (
                :search IS NULL
                OR LOWER(u.originalUrl) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.shortCode) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.customAlias) LIKE LOWER(CONCAT('%', :search, '%'))
              )
          AND (
                :active IS NULL
                OR u.active = :active
              )
    """)
    Page<Url> findByUserIdAndSearchTermAndActive(
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.id = :id")
    void incrementClickCount(@Param("id") Long id);
}