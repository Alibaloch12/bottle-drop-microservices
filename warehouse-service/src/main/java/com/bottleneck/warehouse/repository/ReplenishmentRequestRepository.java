package com.bottleneck.warehouse.repository;

import com.bottleneck.warehouse.entity.ReplenishmentRequest;
import com.bottleneck.warehouse.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplenishmentRequestRepository extends JpaRepository<ReplenishmentRequest, Long> {

    List<ReplenishmentRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
}
