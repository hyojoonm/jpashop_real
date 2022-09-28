package jpabook.jpashop_real.service;

import jpabook.jpashop_real.domain.Delivery;
import jpabook.jpashop_real.domain.Member;
import jpabook.jpashop_real.domain.Order;
import jpabook.jpashop_real.domain.OrderItem;
import jpabook.jpashop_real.domain.item.Item;
import jpabook.jpashop_real.repository.ItemRepository;
import jpabook.jpashop_real.repository.MemberRepository;
import jpabook.jpashop_real.repository.OrderRepository;
import jpabook.jpashop_real.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;


    // 주문
    // 주문 할떄 회원아이디 , 상품아이디 , 주문 수량만 넘어옴
    @Transactional
    public Long order(Long memberId , Long itemId , int count){

        // 엔티티 조회
       Member member = memberRepository.findOne(memberId);
       Item item = itemRepository.findOne(itemId);

        // 배송정보 생성
        // 회원에 주소게 있는 값으로 배송을 함
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // 주문 상품 생성

        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        //주문 저장
        // orderItem 은 CasecadeType이 ALL 이라서 오더에 퍼시스트 하면 오더아이템에도 퍼시스트가 된다
        // 딜리버리도 똑같음 save 한번만 해도 다 저장됨
        // 캐스트 케이드는 다른 곳에서 참조를 하면 사용하지말고 한 곳에서만 참조하면 케스트케이드 사용용
        orderRepository.save(order);

        return order.getId();
    }

    // 취소
    // 값이 변경되면 트랜잭션 무조건 필수!
    @Transactional
    public void cancelOrder(Long orderId){
        // 주문 엔티티 조회

        Order order = orderRepository.findOne(orderId);

        // 주문 취소
        order.cancel();

    }

    // 검색
    public List<Order> findOrders(OrderSearch orderSearch){
        return orderRepository.findAllByString(orderSearch);
    }

}
