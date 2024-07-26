package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {



    //public Member findByMemberId(String memberId);


    public List<Member> findAll();


    @Query("select m from Member m where m.memberId = :memberId")
    Member findByMemberId(@Param("memberId") String memberId);
    @Query("SELECT m FROM Member m WHERE m.memberId = :memberId AND m.password = :password")
    Member findByMemberIdAndPassword(@Param("memberId") String memberId, @Param("password") String password);
}
