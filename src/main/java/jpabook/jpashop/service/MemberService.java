package jpabook.jpashop.service;

import jpabook.jpashop.bizException.BizException;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional

public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public String join(Member member) {
        try {
            // 중복 회원 검증
            if (validateDuplicateMember(member.getMemberId())) {
                throw new IllegalStateException("이미 존재하는 회원입니다.");
            }
            // 회원 저장
            memberRepository.save(member);
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
            return member.getName();
        } else {
            throw new BizException("로그인 실패");
        }
    }
}
