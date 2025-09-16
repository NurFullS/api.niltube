package com.example.server.repository;

import com.example.server.model.Video;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query("SELECT v FROM Video v WHERE LOWER(v.videoName) = LOWER(:videoName)")
    Optional<Video> findByVideoNameIgnoreCase(@Param("videoName") String videoName);

    @Query("SELECT v FROM Video v " +
            "WHERE LOWER(v.videoName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(v.videoDescription) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Video> searchVideos(@Param("query") String query, Pageable pageable);

    List<Video> findByOwnerUsername(String username);
}
