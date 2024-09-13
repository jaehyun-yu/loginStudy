package jpabook.jpashop.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jpabook.jpashop.bizException.BizException;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.jwt.JwtTokenProvider;
import jpabook.jpashop.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String join(Member member) {
        try {
            // 중복 회원 검증
            if (validateDuplicateMember(member.getMemberId())) {
                throw new IllegalStateException("이미 존재하는 회원입니다.");
            }
            // 회원 저장
            memberRepository.save(member);
            //memberRepository.save(Member.builder()
            //                .roles(Collections.singletonList("ROLE_USER"))
            //        .build());
            return member.getMemberId();
        } catch (Exception e) {
            // 예외 발생 시 로깅 및 재전파
            Throwable cause = e.getCause();
            if (cause != null) {
                cause.printStackTrace(); // 또는 로깅
            }
            throw e; // 예외를 다시 던질 수 있습니다.
        }
    }

    // 회원 중복 확인
    public boolean validateDuplicateMember(String memberId) {
        Member existingMember = memberRepository.findByMemberId(memberId);
        boolean isDuplicate = existingMember != null;
        System.out.println("Member ID: " + memberId + " isDuplicate: " + isDuplicate);
        return isDuplicate;
    }

    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(String memberId) {
        return memberRepository.findByMemberId(memberId);
    }

    public String login(String id, String password) {
        Member member = memberRepository.findByMemberIdAndPassword(id, password);
        if (member != null) {
            //return member.getName();
            //jwtTokenProvider.createToken(member.getUsername(), member.getRoles());
            String name = member.getName() + "@" + jwtTokenProvider.getRemainingTime(jwtTokenProvider.createToken(member.getUsername(), member.getRoles()));
            return name;
        } else {
            throw new BizException("로그인 실패");
        }
    }


    public Map<String, Object> verifyToken(String authHeader) {
        Map<String, Object> response = new HashMap<>();
        String token = authHeader.replace("Bearer" , "");

        boolean isValid = jwtTokenProvider.vallidateToken(token);
        response.put("valid", isValid);

        return response;
    }

    public String doSocialLogin(String code , Model model) {

        String accessToken = getKaKaoAccessToken(code);

        model.addAttribute("accessToken" , accessToken);
        String userInfo = getKakaoUserInfo(accessToken);


        return userInfo;
    }

    private String getKaKaoAccessToken(String code) {
        String accessToken = "";
        String refreshToken = "";
        String reqUrl = "https://kauth.kakao.com/oauth/token";

        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=cfffb1896b0309d7bf30e8155a4f1110"); // REST API 키
            sb.append("&redirect_uri=http://localhost:8080/kakao-login");
            sb.append("&code=").append(code);
            bw.write(sb.toString());
            bw.flush();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                StringBuilder result = new StringBuilder();
                while((line = br.readLine()) != null) {
                    result.append(line);
                }

                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(result.toString());

                accessToken = element.getAsJsonObject().get("access_token").getAsString();
                refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

                br.close();
                bw.close();
            } else {
                throw new BizException("인증실패");
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return accessToken;
    }

    private String getKakaoUserInfo(String accessToken) {
        String reqURL =  "https://kapi.kakao.com/v2/user/me";
        String userName = "";

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK ) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                StringBuilder result = new StringBuilder();

                while((line = br.readLine()) != null) {
                    result.append(line);
                }

                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(result.toString());

                if(element.getAsJsonObject().has("properties")) {
                    JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
                    if(properties.has("nickname")) {
                        userName = properties.get("nickname").getAsString();
                    }
                }
                br.close();
            } else {
                throw new BizException("카카오톡 유저정보 가져오기 실패.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return userName;
    }

    public void doSocialLogout(String accessToken) {

        String reqURL = "https://kapi.kakao.com/v1/user/logout";

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
            } else {
                reqURL = "https://kapi.kakao.com/v1/user/access_token_info";

                url = new URL(reqURL);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);

                responseCode = conn.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK) {
                    throw new BizException("토큰은 유효한데 로그아웃 실패");
                } else {
                    throw new BizException("토큰 유효성도 날라간 상태");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
