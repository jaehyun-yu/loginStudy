package jpabook.jpashop.loginControllor;

import jpabook.jpashop.bizException.BizException;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Controller
public class loginController {

    private MemberService memberService;

    public loginController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "message", required = false) String message, Model model) {
        if (message != null) {
            model.addAttribute("message", message);
        }
        return "login";
    }

    @GetMapping("/joinpage")
    public String showJoinPage() {
        return "join";
    }

    @GetMapping("/checkresult")
    public String showCheckResult(@RequestParam boolean isUnique, @RequestParam String memberId, Model model) {
        model.addAttribute("isUnique", isUnique);
        model.addAttribute("memberId", memberId);
        return "checkresult";
    }

    @GetMapping("/hello")
    public String showHelloPage(@RequestParam String name, Model model) {
        model.addAttribute("name", name);
        return "hello";
    }

    @GetMapping("/custlogin")
    public String custLogin(@RequestParam String id, @RequestParam String password, Model model) {
        try {
            String name = memberService.login(id, password);
            return showHelloPage(name, model);
        } catch (BizException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @PostMapping("/join")
    public String join(@ModelAttribute Member member, Model model) {
        try {
            memberService.join(member);
            String message = "회원가입이 완료되었습니다.";
            try {
                message = URLEncoder.encode(message, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return "redirect:/login?message=" + message;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "join"; // 회원가입 페이지로 돌아가기
        }
    }

    @GetMapping("/idvali")
    @ResponseBody
    public Map<String, Object> idValicheck(@RequestParam String memberId) {
        boolean isUnique = !memberService.validateDuplicateMember(memberId);  // 중복이 없으면 true, 있으면 false
        Map<String, Object> response = new HashMap<>();
        response.put("isUnique", isUnique);
        return response;
    }

    @PostMapping("api/verifyToken")
    public Map<String, Object> verifyToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = memberService.verifyToken(authHeader);
        return response;
    }

    @GetMapping("kakao-login")
    public String kakaoLogin(@RequestParam("code") String code , Model model) {
        //ResponseEntity.created(URI.create("/kakao-login"))
                //.body(memberService.doSocialLogin(tokenparam, model));
        return showHelloPage(memberService.doSocialLogin(code, model), model);
    }

}
