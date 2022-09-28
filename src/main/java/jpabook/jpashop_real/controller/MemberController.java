package jpabook.jpashop_real.controller;

import jpabook.jpashop_real.domain.Address;
import jpabook.jpashop_real.domain.Member;
import jpabook.jpashop_real.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model){
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping("members/new")
    // BindingResult 를 사용하면 오류를 result에 담아서 오류가 났을때 내가 원하는 로직을 짤 수 있음
    // memberForm 만드는 이유가 타임리프에서 데이터랑 member 엔티티 데이터가 맞지가 않아서 memberForm을 만들고
    // 거기서 딱 필요한 정보만 컨트롤러에서 정제후 member 엔티티에 저장!
    public String create(@Valid MemberForm form , BindingResult result){

        // 오류가 나면 members/createMemberForm 페이지로 이동동
       if ((result.hasErrors())){
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/";
    }

    // 실무에서 엔티티는 핵심 비지니스 로직만 쓰고 화면에 쓰는 객체는 Dto 나 Form 써서 해야함 엔티티는 순수하게 해야 좋음
    // API 만들때 외부로 엔티티를 반환하면 절대 안됨 API 스펙이 변경됨 템플릿 엔진에서는 괜찮음 서버 사이드에서 돌기 떄문에
    @GetMapping("/members")
    public String list(Model model){
        List<Member> members = memberService.findMembers();
        model.addAttribute("members",members);
        return "/members/memberList";
    }

}
