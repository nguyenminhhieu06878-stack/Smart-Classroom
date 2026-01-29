package com.smartclass.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartclass.model.Plan;
import com.smartclass.model.Subscription;
import com.smartclass.model.User;
import com.smartclass.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${payos.client-id}")
    private String clientId;
    
    @Value("${payos.api-key}")
    private String apiKey;
    
    @Value("${payos.checksum-key}")
    private String checksumKey;
    
    @Value("${app.base-url:http://localhost:1002}")
    private String baseUrl;
    
    private static final String PAYOS_API_URL = "https://api-merchant.payos.vn/v2/payment-requests";
    
    /**
     * Create payment link for subscription
     */
    public Map<String, Object> createPaymentLink(User user, Plan plan, int months) {
        Subscription subscription = null;
        long orderCode = 0;
        
        try {
            // Create pending subscription
            subscription = new Subscription();
            subscription.setUser(user);
            subscription.setPlan(plan);
            subscription.setStatus(Subscription.Status.ACTIVE);
            subscription.setPaymentStatus(Subscription.PaymentStatus.PENDING);
            subscription.setStartDate(LocalDateTime.now());
            subscription.setEndDate(LocalDateTime.now().plusMonths(months));
            subscription.setAmount(plan.getPrice() * months);
            subscription.setPaymentMethod("PayOS");
            subscription = subscriptionRepository.save(subscription);
            
            // Generate unique order code
            orderCode = System.currentTimeMillis();
            int amount = plan.getPrice().intValue() * months;
            String description = "Goi " + plan.getName(); // Max 25 characters
            
            // Truncate description if too long
            if (description.length() > 25) {
                description = description.substring(0, 25);
            }
            
            // Create payment request body
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("orderCode", orderCode);
            paymentData.put("amount", amount);
            paymentData.put("description", description);
            paymentData.put("returnUrl", baseUrl + "/teacher/payment/success?subscriptionId=" + subscription.getId());
            paymentData.put("cancelUrl", baseUrl + "/teacher/payment/cancel?subscriptionId=" + subscription.getId());
            
            // Create items
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("name", plan.getName() + " - " + months + " thang");
            item.put("quantity", 1);
            item.put("price", amount);
            items.add(item);
            paymentData.put("items", items);
            
            // Create signature
            String signatureData = "amount=" + amount + "&cancelUrl=" + paymentData.get("cancelUrl") 
                + "&description=" + description + "&orderCode=" + orderCode 
                + "&returnUrl=" + paymentData.get("returnUrl");
            String signature = createSignature(signatureData, checksumKey);
            paymentData.put("signature", signature);
            
            // Call PayOS API
            OkHttpClient client = new OkHttpClient();
            String jsonBody = objectMapper.writeValueAsString(paymentData);
            
            log.info("PayOS Request URL: {}", PAYOS_API_URL);
            log.info("PayOS Request Body: {}", jsonBody);
            log.info("PayOS Client ID: {}", clientId);
            
            RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
            );
            
            Request request = new Request.Builder()
                .url(PAYOS_API_URL)
                .addHeader("x-client-id", clientId)
                .addHeader("x-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
            
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            
            log.info("PayOS API Response Status: {}", response.code());
            log.info("PayOS API Response Body: {}", responseBody);
            
            if (!response.isSuccessful()) {
                log.error("PayOS API error: Status={}, Body={}", response.code(), responseBody);
                throw new RuntimeException("PayOS API error: " + responseBody);
            }
            
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            
            // Check if response has error
            if (result.containsKey("code") && !"00".equals(result.get("code"))) {
                String errorMsg = result.containsKey("desc") ? (String) result.get("desc") : "Unknown error";
                log.error("PayOS returned error: {}", errorMsg);
                throw new RuntimeException("PayOS error: " + errorMsg);
            }
            
            // Get data from response
            if (!result.containsKey("data") || result.get("data") == null) {
                log.error("PayOS response missing data field: {}", responseBody);
                throw new RuntimeException("PayOS response không có dữ liệu");
            }
            
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            
            // Log data structure for debugging
            log.info("PayOS data keys: {}", data.keySet());
            
            // Update subscription with transaction ID
            subscription.setTransactionId(String.valueOf(orderCode));
            subscriptionRepository.save(subscription);
            
            log.info("Created payment link for user {} - subscription {}", user.getEmail(), subscription.getId());
            
            return data;
            
        } catch (Exception e) {
            log.error("Error creating payment link", e);
            throw new RuntimeException("Không thể tạo link thanh toán: " + e.getMessage());
        }
    }
    
    /**
     * Create HMAC SHA256 signature
     */
    private String createSignature(String data, String key) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error creating signature", e);
        }
    }
    
    /**
     * Confirm payment and activate subscription
     */
    @Transactional
    public void confirmPayment(Long subscriptionId, String transactionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new RuntimeException("Subscription not found"));
        
        // Cancel any existing active subscriptions for this user
        List<Subscription> activeSubscriptions = subscriptionRepository
            .findByUserAndStatus(subscription.getUser(), Subscription.Status.ACTIVE);
        
        for (Subscription activeSub : activeSubscriptions) {
            if (!activeSub.getId().equals(subscriptionId)) {
                activeSub.setStatus(Subscription.Status.CANCELLED);
                subscriptionRepository.save(activeSub);
            }
        }
        
        // Activate new subscription
        subscription.setPaymentStatus(Subscription.PaymentStatus.PAID);
        subscription.setStatus(Subscription.Status.ACTIVE);
        subscription.setTransactionId(transactionId);
        subscriptionRepository.save(subscription);
        
        // Update user's current plan
        User user = subscription.getUser();
        user.setCurrentPlan(subscription.getPlan());
        
        log.info("Payment confirmed for subscription {}", subscriptionId);
    }
    
    /**
     * Cancel payment
     */
    @Transactional
    public void cancelPayment(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new RuntimeException("Subscription not found"));
        
        subscription.setPaymentStatus(Subscription.PaymentStatus.FAILED);
        subscription.setStatus(Subscription.Status.CANCELLED);
        subscriptionRepository.save(subscription);
        
        log.info("Payment cancelled for subscription {}", subscriptionId);
    }
    
    /**
     * Get active subscription for user
     */
    public Subscription getActiveSubscription(User user) {
        return subscriptionRepository.findByUserAndStatus(user, Subscription.Status.ACTIVE)
            .stream()
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Check if user can use AI features
     * TEMPORARY: Always return true for testing (bypass payment)
     */
    public boolean canUseAILesson(User user) {
        // TODO: Uncomment this when ready for production
        // return subscriptionRepository.findByUserAndStatus(user, Subscription.Status.ACTIVE)
        //     .stream()
        //     .findFirst()
        //     .map(sub -> {
        //         Integer used = sub.getAiLessonsUsed() != null ? sub.getAiLessonsUsed() : 0;
        //         Integer limit = sub.getPlan().getAiLessonsPerMonth();
        //         return limit == null || limit == -1 || used < limit;
        //     })
        //     .orElse(false);
        
        // For testing: always allow
        return true;
    }
    
    public boolean canUseAITest(User user) {
        // TODO: Uncomment this when ready for production
        // return subscriptionRepository.findByUserAndStatus(user, Subscription.Status.ACTIVE)
        //     .stream()
        //     .findFirst()
        //     .map(sub -> {
        //         Integer used = sub.getAiTestsUsed() != null ? sub.getAiTestsUsed() : 0;
        //         Integer limit = sub.getPlan().getTestsPerMonth();
        //         return limit == null || limit == -1 || used < limit;
        //     })
        //     .orElse(false);
        
        // For testing: always allow
        return true;
    }
    
    public boolean canCreateTest(User user) {
        // TODO: Uncomment this when ready for production
        // return subscriptionRepository.findByUserAndStatus(user, Subscription.Status.ACTIVE)
        //     .stream()
        //     .findFirst()
        //     .map(sub -> {
        //         Integer used = sub.getTestsCreated() != null ? sub.getTestsCreated() : 0;
        //         Integer limit = sub.getPlan().getTestsPerMonth();
        //         return limit == null || limit == -1 || used < limit;
        //     })
        //     .orElse(false);
        
        // For testing: always allow
        return true;
    }
    
    /**
     * Track AI usage
     */
    @Transactional
    public void trackAILessonUsage(User user) {
        subscriptionRepository.findByUserAndStatus(user, Subscription.Status.ACTIVE)
            .stream()
            .findFirst()
            .ifPresent(sub -> {
                sub.incrementAiLessonsUsed();
                subscriptionRepository.save(sub);
            });
    }
    
    @Transactional
    public void trackAIQuestionsUsage(User user, int count) {
        subscriptionRepository.findByUserAndStatus(user, Subscription.Status.ACTIVE)
            .stream()
            .findFirst()
            .ifPresent(sub -> {
                sub.incrementAiQuestionsUsed(count);
                subscriptionRepository.save(sub);
            });
    }
    
    @Transactional
    public void trackAITestUsage(User user) {
        subscriptionRepository.findByUserAndStatus(user, Subscription.Status.ACTIVE)
            .stream()
            .findFirst()
            .ifPresent(sub -> {
                sub.incrementAiTestsUsed();
                subscriptionRepository.save(sub);
            });
    }
    
    @Transactional
    public void trackTestCreation(User user) {
        subscriptionRepository.findByUserAndStatus(user, Subscription.Status.ACTIVE)
            .stream()
            .findFirst()
            .ifPresent(sub -> {
                sub.incrementTestsCreated();
                subscriptionRepository.save(sub);
            });
    }
}
