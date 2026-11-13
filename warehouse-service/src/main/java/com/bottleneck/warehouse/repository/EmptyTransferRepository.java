package com.bottleneck.warehouse.repository;

import com.bottleneck.warehouse.entity.EmptyTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmptyTransferRepository extends JpaRepository<EmptyTransfer, Long> {

    List<EmptyTransfer> findAllByOrderByReceivedAtDesc();
}
