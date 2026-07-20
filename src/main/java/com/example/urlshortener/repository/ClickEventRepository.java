package com.example.urlshortener.repository;

import com.example.urlshortener.entity.ClickEvent;
import com.example.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    @Query("SELECT COUNT(c) FROM ClickEvent c WHERE c.url.id = :urlId")
    long countByUrlId(@Param("urlId") Long urlId);

    @Query("SELECT COUNT(c) FROM ClickEvent c WHERE c.url.id = :urlId AND c.clickedAt >= :from")
    long countByUrlIdSince(@Param("urlId") Long urlId, @Param("from") Instant from);

    @Query("SELECT MIN(c.clickedAt) FROM ClickEvent c WHERE c.url.id = :urlId")
    Optional<Instant> findFirstClickDateByUrlId(@Param("urlId") Long urlId);

    @Query("SELECT MAX(c.clickedAt) FROM ClickEvent c WHERE c.url.id = :urlId")
    Optional<Instant> findLastClickDateByUrlId(@Param("urlId") Long urlId);

    List<ClickEvent> findTop10ByUrlOrderByClickedAtDesc(Url url);

    @Query("SELECT COUNT(c) FROM ClickEvent c")
    long countAllClicks();
}
