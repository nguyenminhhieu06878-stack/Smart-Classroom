package com.smartclass.service;

import com.smartclass.model.Plan;
import com.smartclass.model.Subscription;
import com.smartclass.model.User;
import com.smartclass.repository.PlanRepository;
import com.smartclass.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, PlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
    }

    // Plan methods
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    public List<Plan> getActivePlans() {
        return planRepository.findByActiveTrue();
    }

    public Optional<Plan> getPlanById(Long id) {
        return planRepository.findById(id);
    }

    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    public Optional<Subscription> getActiveSubscription(User user) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserAndStatus(user, Subscription.Status.ACTIVE);
        return subscriptions.isEmpty() ? Optional.empty() : Optional.of(subscriptions.get(0));
    }

    public Optional<Subscription> getLatestSubscription(User user) {
        return subscriptionRepository.findFirstByUserOrderByEndDateDesc(user);
    }

    public List<Subscription> getActiveSubscriptions(User user) {
        return subscriptionRepository.findByUserAndStatus(user, Subscription.Status.ACTIVE);
    }

    public void saveSubscription(Subscription subscription) {
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription createSubscription(User user, Plan plan, int months) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus(Subscription.Status.ACTIVE);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(months));

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        subscription.setStatus(Subscription.Status.CANCELLED);
        subscriptionRepository.save(subscription);
    }

    public long countActiveSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == Subscription.Status.ACTIVE)
                .count();
    }
}
