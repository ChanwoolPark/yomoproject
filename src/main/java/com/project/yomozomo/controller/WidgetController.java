package com.project.yomozomo.controller;

import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.repository.UserWalletRepository;
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

@Controller
public class WidgetController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final UserWalletRepository userWalletRepository;

    public WidgetController(UserRepository userRepository, UserWalletRepository userWalletRepository) {
        this.userRepository = userRepository;
        this.userWalletRepository = userWalletRepository;
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

        // TODO: 개발자센터에 로그인해서 내 결제위젯 연동 키 > 시크릿 키를 입력하세요. 시크릿 키는 외부에 공개되면 안돼요.
        // @docs https://docs.tosspayments.com/reference/using-api/api-keys
        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";

        // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용하고, 비밀번호는 사용하지 않습니다.
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가합니다.
        // @docs https://docs.tosspayments.com/reference/using-api/authorization#%EC%9D%B8%EC%A6%9D
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        // 결제 승인 API를 호출하세요.
        // 결제를 승인하면 결제수단에서 금액이 차감돼요.
        // @docs https://docs.tosspayments.com/guides/v2/payment-widget/integration#3-결제-승인하기
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

        // TODO: 결제 성공 및 실패 비즈니스 로직을 구현하세요.
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

        // 지갑 정보 가져오기 (UserWallet, 예시)
        UserWallet wallet = userWalletRepository.findByUserId(user.getId());
        if (wallet == null) {
            wallet = new UserWallet();
            wallet.setUserId(user.getId());
            wallet.setBalance(0);
        }
        // 충전 금액 반영
        wallet.setBalance(wallet.getBalance() + amount);
        userWalletRepository.save(wallet);

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
