package com.purchase_service.Bottle.Drop.System.repository;

import com.purchase_service.Bottle.Drop.System.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    Page<Purchase> findByVisitorId(Long visitorId, Pageable pageable);

    Page<Purchase> findByBarId(Long barId, Pageable pageable);

    Page<Purchase> findByVisitorIdAndBarId(Long visitorId, Long barId, Pageable pageable);
}
