package jpabook.jpashop_real.controller;

import jpabook.jpashop_real.domain.Member;
import jpabook.jpashop_real.domain.Order;
import jpabook.jpashop_real.domain.item.Item;
import jpabook.jpashop_real.repository.OrderSearch;
import jpabook.jpashop_real.service.ItemService;
import jpabook.jpashop_real.service.MemberService;
import jpabook.jpashop_real.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping("/order")
    public String createForm(Model model){
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("members",members);
        model.addAttribute("items",items);

        return "order/orderForm";
    }
    // @RequestParam 은 form 방식에서 orderForm 에서 넘어오는 데이터들을 선언
    @PostMapping("/order")
    public String order(@RequestParam("memberId") Long memberId,
                        @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count){
        // 밖에서 엔티티를 찾아서 여기서 값을 넣어도 좋지만 이렇게 식별자만 넘겨주고 핵심 로직을 서비스에서 처리하면
        // 영속성 컨테스트가 존재하는 서비스 클래스에서 값이 변경이 가능하고 더티 체킹이 자연스럽게 적용 됨
        // 밖에서 가지고 들어오면 Transactional 없는 곳에서 조회하면 값이 변경이 안 될 수도 있음
        orderService.order(memberId, itemId, count);
        return "redirect:/orders";
    }

    // 화면에 단순하게 조회하는 기능이면 컨트롤러에서 레포지토리 호출해서 해도 괜찮
    // findOrders 는 그냥 위임
   @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model){
        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders",orders);

        return "order/orderList";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId){
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }

}
