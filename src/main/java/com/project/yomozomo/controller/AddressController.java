package com.project.yomozomo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

@Controller
@RequestMapping("/address")
public class AddressController {
    @Value("${juso.serviceKey}")
    private String serviceKey;

    //  (1) 검색용 GET
    @GetMapping("/popup")
    public String popup(Model model) {
        model.addAttribute("jusoKey", serviceKey);
        return "address_popup";       // 검색 화면 + init()
    }

    //  (2) 콜백용 POST
    @PostMapping("/popup")
    public String callback(
            @RequestParam("zipNo") String zipNo,
            @RequestParam("roadAddrPart1") String roadAddrPart1,
            Model model
    ) {
        model.addAttribute("zipNo", zipNo);
        model.addAttribute("roadAddrPart1", roadAddrPart1);
        return "address_popup_callback";   // 바로 부모창에 전달 후 종료
    }

    @GetMapping("/search")
    @ResponseBody
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "1") int page) {
        // RestTemplate이나 WebClient로 juso.go.kr API 호출
        String url = "https://www.juso.go.kr/addrlink/addrLinkApi.do"
                + "?confmKey=" + serviceKey
                + "&currentPage=" + page
                + "&countPerPage=10"
                + "&keyword=" + UriUtils.encode(keyword, "UTF-8")
                + "&resultType=json";
        RestTemplate rt = new RestTemplate();
        return rt.getForObject(url, String.class);
    }
}
