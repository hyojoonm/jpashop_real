package jpabook.jpashop_real.repository.order.simplequery;

import jpabook.jpashop_real.domain.Address;
import jpabook.jpashop_real.domain.Order;
import jpabook.jpashop_real.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate; //주문시간
    private OrderStatus orderStatus;
    private Address address;
    // Dto 가 엔티티를 받는건 상관없음 별 중요하지 않는 곳에서 의존하는 것이기 때문에
    public OrderSimpleQueryDto(Long orderId , String name , LocalDateTime orderDate ,OrderStatus orderStatus , Address address) {
        this.orderId = orderId;
        this.name = name; // LAZY 초기화 영속성 컨테스트에 찾아보는데 네임이 없으면 DB 쿼리를 날림
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;  // LAZY 초기화 영속성 컨테스트에 찾아보는데 네임이 없으면 DB 쿼리를 날림
    }
}