//package com.smartclass.controller;
//
//import com.smartclass.service.PaymentService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//@Controller
//@RequestMapping("/teacher/payment")
//@Slf4j
//public class PaymentController {
//
//    private final PaymentService paymentService;
//
//    public PaymentController(PaymentService paymentService) {
//        this.paymentService = paymentService;
//    }
//
//    @GetMapping("/success")
//    public String paymentSuccess(
//            @RequestParam Long subscriptionId,
//            @RequestParam(required = false) String orderCode,
//            RedirectAttributes redirectAttributes) {
//
//        try {
//            paymentService.confirmPayment(subscriptionId, orderCode);
//            redirectAttributes.addFlashAttribute("successMessage",
//                    "Thanh toán thành công! Gói dịch vụ của bạn đã được kích hoạt.");
//            log.info("Payment success for subscription: {}", subscriptionId);
//        } catch (Exception e) {
//            log.error("Error confirming payment", e);
//            redirectAttributes.addFlashAttribute("errorMessage",
//                    "Có lỗi xảy ra khi xác nhận thanh toán. Vui lòng liên hệ hỗ trợ.");
//        }
//
//        return "redirect:/teacher/subscription";
//    }
//
//    @GetMapping("/mock")
//    public String paymentMock(
//            @RequestParam Long subscriptionId,
//            @RequestParam Double amount,
//            @RequestParam String planName,
//            @RequestParam int months,
//            @RequestParam String orderCode,
//            Model model) {
//
//        model.addAttribute("subscriptionId", subscriptionId);
//        model.addAttribute("amount", amount);
//        model.addAttribute("planName", planName);
//        model.addAttribute("months", months);
//        model.addAttribute("orderCode", orderCode);
//
//        return "teacher/payment-mock";
//    }
//
//    @GetMapping("/cancel")
//    public String paymentCancel(
//            @RequestParam Long subscriptionId,
//            RedirectAttributes redirectAttributes) {
//
//        try {
//            paymentService.cancelPayment(subscriptionId);
//            redirectAttributes.addFlashAttribute("errorMessage",
//                    "Thanh toán đã bị hủy. Vui lòng thử lại.");
//            log.info("Payment cancelled for subscription: {}", subscriptionId);
//        } catch (Exception e) {
//            log.error("Error cancelling payment", e);
//        }
//
//        return "redirect:/teacher/subscription";
//    }
//
//    @GetMapping("/test-payos")
//    public String testPayOS(Model model) {
//        try {
//            // Test PayOS API directly
//            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
//
//            String clientId = "da7502de-7fa2-4c39-9e36-710f0709d227";
//            String apiKey = "54e6c8fe-1005-467e-9222-aa61bf35e0b7";
//
//            java.util.Map<String, Object> testData = new java.util.HashMap<>();
//            testData.put("orderCode", System.currentTimeMillis());
//            testData.put("amount", 10000);
//            testData.put("description", "Test payment");
//            testData.put("returnUrl", "http://localhost:1002/teacher/payment/success");
//            testData.put("cancelUrl", "http://localhost:1002/teacher/payment/cancel");
//
//            java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
//            java.util.Map<String, Object> item = new java.util.HashMap<>();
//            item.put("name", "Test item");
//            item.put("quantity", 1);
//            item.put("price", 10000);
//            items.add(item);
//            testData.put("items", items);
//
//            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
//            String jsonBody = mapper.writeValueAsString(testData);
//
//            model.addAttribute("requestInfo", "URL: https://api-merchant.payos.vn/v2/payment-requests\n" +
//                    "Client ID: " + clientId + "\n" +
//                    "Request Body:\n" + jsonBody);
//
//            okhttp3.RequestBody body = okhttp3.RequestBody.create(
//                    jsonBody,
//                    okhttp3.MediaType.parse("application/json"));
//
//            okhttp3.Request request = new okhttp3.Request.Builder()
//                    .url("https://api-merchant.payos.vn/v2/payment-requests")
//                    .addHeader("x-client-id", clientId)
//                    .addHeader("x-api-key", apiKey)
//                    .addHeader("Content-Type", "application/json")
//                    .post(body)
//                    .build();
//
//            okhttp3.Response response = client.newCall(request).execute();
//            String responseBody = response.body().string();
//
//            model.addAttribute("responseStatus", response.code());
//            model.addAttribute("responseBody", responseBody);
//
//        } catch (Exception e) {
//            model.addAttribute("error", e.getMessage() + "\n" +
//                    java.util.Arrays.toString(e.getStackTrace()));
//        }
//
//        return "teacher/test-payos";
//    }
//}
