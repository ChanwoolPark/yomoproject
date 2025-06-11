package com.project.yomozomo.controller;

import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.domain.WalletLog;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.repository.UserWalletRepository;
import com.project.yomozomo.repository.WalletLogRepository;
import com.project.yomozomo.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.Date;

@Controller
public class WidgetController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final UserWalletRepository userWalletRepository;
    private final WalletLogRepository walletLogRepository;
    private final WalletService walletService;

    public WidgetController(UserRepository userRepository, UserWalletRepository userWalletRepository, WalletLogRepository walletLogRepository, WalletService walletService) {
        this.userRepository = userRepository;
        this.userWalletRepository = userWalletRepository;
        this.walletLogRepository = walletLogRepository;
        this.walletService = walletService;
    }

    @PostMapping("/widget")
    public String widgetCharge(HttpServletRequest request, Principal principal, Model model) {
        // 충전 폼에서 넘어온 값 받기
        String chargeAmountStr = request.getParameter("chargeAmount");
        int chargeAmount = Integer.parseInt(chargeAmountStr);

        // 로그인 사용자 정보 조회
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // (상품명은 고정값이거나, 필요시 폼에서 받아오기)
        String productName = "요모페이 포인트 충전";

        // 모델에 값 담아서 checkout.html로 넘김
        model.addAttribute("chargeAmount", chargeAmount);
        model.addAttribute("userNickname", user.getNickname());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("productName", productName);

        return "checkout";
    }
    @RequestMapping(value = "/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody) throws Exception {

        JSONParser parser = new JSONParser();
        String orderId;
        String amount;
        String paymentKey;
        try {
            // 클라이언트에서 받은 JSON 요청 바디입니다.
            JSONObject requestData = (JSONObject) parser.parse(jsonBody);
            paymentKey = (String) requestData.get("paymentKey");
            orderId = (String) requestData.get("orderId");
            amount = (String) requestData.get("amount");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        };
        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);


        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";


        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);


        URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);


        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes("UTF-8"));

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;

        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();


        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        responseStream.close();

        return ResponseEntity.status(code).body(jsonObject);
    }

    /**
     * 인증성공처리
     * @param request
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping("/success")
    public String paymentRequest(HttpServletRequest request, Model model, Principal principal) {
        String orderId = request.getParameter("orderId");
        String amountStr = request.getParameter("amount");
        int amount = 0;
        if (amountStr != null) {
            amount = Integer.parseInt(amountStr);
        }

        // 현재 로그인한 유저 정보 가져오기
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 지갑 정보 가져오기 (UserWallet)
        UserWallet wallet = userWalletRepository.findByUserId(user.getId());
        if (wallet == null) {
            wallet = new UserWallet();
            wallet.setUserId(user.getId());
            wallet.setBalance(0);
        }
        // 충전 금액 반영
        wallet.setBalance(wallet.getBalance() + amount);
        userWalletRepository.save(wallet);

        // 1️⃣ [여기서 WalletLog 저장!]
        WalletLog log = new WalletLog();
        log.setUserWalletId(wallet.getWalletId()); // PK
        log.setAmount(amount);
        log.setType("충전");
        log.setCreatedAt(new Date());
        walletLogRepository.save(log);


        walletService.updateGradeIfNeeded(user.getId(), wallet.getBalance());

        // 성공 메시지/금액 모델에 추가
        model.addAttribute("amount", amount);
        model.addAttribute("walletBalance", wallet.getBalance());

        return "success"; // success.html
    }

    @RequestMapping(value = "/widget", method = RequestMethod.GET)
    public String index(HttpServletRequest request, Model model) throws Exception {
        return "checkout";
    }

    /**
     * 인증실패처리
     * @param request
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/fail", method = RequestMethod.GET)
    public String failPayment(HttpServletRequest request, Model model) throws Exception {
        String failCode = request.getParameter("code");
        String failMessage = request.getParameter("message");

        model.addAttribute("code", failCode);
        model.addAttribute("message", failMessage);

        return "/fail";
    }
}
