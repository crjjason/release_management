package com.releasemgmt.repository;

import com.releasemgmt.model.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseRepository extends JpaRepository<Release, Long> {

    @Query("SELECT r FROM Release r LEFT JOIN FETCH r.releaseArtifacts ra LEFT JOIN FETCH ra.component LEFT JOIN FETCH r.sitEnvironment LEFT JOIN FETCH r.uatEnvironment")
    List<Release> findAllWithArtifactsAndEnvironments();

    @Query("SELECT r FROM Release r LEFT JOIN FETCH r.releaseArtifacts ra LEFT JOIN FETCH ra.component LEFT JOIN FETCH r.sitEnvironment LEFT JOIN FETCH r.uatEnvironment WHERE r.id = :id")
    Optional<Release> findByIdWithArtifactsAndEnvironments(Long id);
}
