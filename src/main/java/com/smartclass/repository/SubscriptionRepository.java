package com.smartclass.repository;

import com.smartclass.model.Subscription;
import com.smartclass.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUser(User user);
    List<Subscription> findByStatus(Subscription.Status status);
    List<Subscription> findByUserAndStatus(User user, Subscription.Status status);
    Optional<Subscription> findFirstByUserOrderByEndDateDesc(User user);
    
    @Query("SELECT s FROM Subscription s WHERE s.createdAt >= :startDate ORDER BY s.createdAt DESC")
    List<Subscription> findRecentSubscriptions(LocalDateTime startDate);
    
    @Query("SELECT FUNCTION('DATE', s.createdAt) as date, SUM(s.amount) as revenue " +
           "FROM Subscription s " +
           "WHERE s.createdAt >= :startDate " +
           "GROUP BY FUNCTION('DATE', s.createdAt) " +
           "ORDER BY date")
    List<Object[]> getRevenueByDate(LocalDateTime startDate);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = :status")
    long countByStatus(Subscription.Status status);
    
    default long countActiveSubscriptions() {
        return countByStatus(Subscription.Status.ACTIVE);
    }
    
    @Query("SELECT SUM(s.amount) FROM Subscription s WHERE s.createdAt >= :startDate")
    Double getTotalRevenue(LocalDateTime startDate);
}
