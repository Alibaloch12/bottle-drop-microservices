package com.example.barmanagement.domain.repository;

import com.example.barmanagement.domain.ReplenishmentRequest;
import com.example.barmanagement.domain.ReplenishmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReplenishmentRequestRepository
        extends JpaRepository<
        ReplenishmentRequest,
        Long
        > {

    @Query("""
        select request
        from ReplenishmentRequest request
        where request.barId = :barId
          and request.beverageType.id = :beverageTypeId
          and request.status in :statuses
        """)
    Optional<ReplenishmentRequest> findOpenRequest(
            @Param("barId")
            Long barId,

            @Param("beverageTypeId")
            Long beverageTypeId,

            @Param("statuses")
            Collection<ReplenishmentStatus> statuses
    );

    List<ReplenishmentRequest>
    findByBarIdOrderByRequestedAtDesc(
            Long barId
    );

    long deleteByBarId(Long barId);
}