package jpabook.jpashop_real.service;

import jpabook.jpashop_real.domain.Member;
import jpabook.jpashop_real.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
// 읽기 에는 readOnly = true 데이터 변경할 필요가 없으니
// jpa 조회하는 곳에서 성능 최적화 가능 영속성 컨테스트를 플러쉬 더티채킹 안해서 읽기전용이라 성능이 좋음
@Transactional(readOnly = true) // jpa는 트랜잭션 안에서 데이터 변경이 이루어져야 함
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    // 생성자 주입 방식 권장 변경 불가능한 안전한 객체 생성 가능
    // 생성자 하나면 생략 가능 이 코드를 @RequiredArgsConstructor 로 간단하게 설정 가능!
//    @Autowired
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    //회원 가입
    @Transactional
    public Long join (Member member){

        validateDuplicateMember(member); // 중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    // 중복 회원 검증
    private void validateDuplicateMember(Member member) {
        // 예외 터트리기 EXCEPTION
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    //회원 전체 조회
    public List<Member> findMember(){
        return memberRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }
}
