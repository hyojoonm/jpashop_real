package jpabook.jpashop_real.api;

import jpabook.jpashop_real.domain.Address;
import jpabook.jpashop_real.domain.Order;
import jpabook.jpashop_real.domain.OrderItem;
import jpabook.jpashop_real.domain.OrderStatus;
import jpabook.jpashop_real.repository.OrderRepository;
import jpabook.jpashop_real.repository.OrderSearch;
import jpabook.jpashop_real.repository.order.query.OrderFlatDto;
import jpabook.jpashop_real.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop_real.repository.order.query.OrderQueryDto;
import jpabook.jpashop_real.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
/*
주문내역에서 추가로 주문한 상품 정보를 추가로 조회하자.
Order 기준으로 컬렉션인 OrderItem 와 Item 이 필요하다.
앞의 예제에서는 toOne(OneToOne, ManyToOne) 관계만 있었다. 이번에는 컬렉션인 일대다 관계
(OneToMany)를 조회하고, 최적화하는 방법을 알아보자.
 */

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    /*
orderItem , item 관계를 직접 초기화하면 Hibernate5Module 설정에 의해 엔티티를 JSON으로 생성한다.
양방향 연관관계면 무한 루프에 걸리지 않게 한곳에 @JsonIgnore 를 추가해야 한다
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        // 검색조건 없이 다 가져옴 findAllByString
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {

            order.getMember().getName(); // LAZY 지연로딩 강제 초기화
            order.getDelivery().getAddress(); // LAZY 지연로딩 강제 초기화

            List<OrderItem> orderItems = order.getOrderItems(); // LAZY 지연로딩 강제 초기화
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return collect;
    }
    // 패치 조인 하나로 쿼리 수가 달라짐
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return collect;
    }
    /*
    v3은 한꺼번에 쿼리를 날리지만 DB에서 중복된 정보들이 뻥튀키가 돼서 어플리케이션으로 보낸다 (용량이 크다)
    컬렉션 패치 조인이 불가능!

    v3.1 1. 주문이 2건이라 그냥 2개만 DB에서 조회가 된다 데이터 값이 중복이 없다 (데이터 전송량이 적다)
        2. orderItem 조회시 데이터 중복이 없다.
        3. item 조회시 데이터 중복이 없다.
        테이블 단위로 데이터를 딱 가져오는거라 데이터 중복이 없다. (정규화가 되어있다 대신 쿼리가 한개가 아니다)
        페이징이 가능 !!

        데이터를 보낼때 단 쿼리 (데이터 양이 적은 것)는 성능에 상관이 없는데 데이터를 한 번에 천개를 끌어올릴때 는
        패치 조인으로 한번에 끌고 오는 것 보다 이렇게  배치 패치 사이즈를 설정하는게 성능이 더 좋을 수가 있다.
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
                @RequestParam (value = "offset",defaultValue = "0") int offset,
                @RequestParam (value = "limit" ,defaultValue = "100") int limit) {
        // toOne 관계 애들은 패치조인으로 한번에 다 가져옴 (페이징에 영향을 주지 않음)
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset,limit);

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return collect;
    }


    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5(){
        return orderQueryRepository.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        Collectors.mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }


    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;
        // 엔티티를 직접적으로 노출하면 안되기 때문에 OrderItem 도 DTO로 변환해야 한다. 그래야 위험성을 낮출 수 있다.
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate =order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());

        }
    }

    @Getter
    static class OrderItemDto{

        private String itemName; // 상품명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
