package com.releasemgmt.repository;

import com.releasemgmt.model.ReleaseArtifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReleaseArtifactRepository extends JpaRepository<ReleaseArtifact, Long> {
    List<ReleaseArtifact> findByReleaseId(Long releaseId);
}
