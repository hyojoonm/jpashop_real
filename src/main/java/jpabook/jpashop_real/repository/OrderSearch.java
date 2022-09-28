package jpabook.jpashop_real.repository;

import jpabook.jpashop_real.domain.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearch {

    private String memberName; // 회원 이름
    private OrderStatus orderStatus; // 주문 상태 [ORDER, CANCEL]
}
