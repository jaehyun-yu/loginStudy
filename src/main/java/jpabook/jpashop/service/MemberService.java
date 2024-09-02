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
}
