package com.releasemgmt.repository;

import com.releasemgmt.model.Deployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, Long> {
    List<Deployment> findByEnvironmentId(Long environmentId);

    Optional<Deployment> findByEnvironmentIdAndComponentId(Long environmentId, Long componentId);
}
