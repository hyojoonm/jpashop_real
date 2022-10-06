package jpabook.jpashop_real.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop_real.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; // 주문 가격
    private int count; // 주문 수량

    // new 생성자를 막기위해서 추가함
    // @NoArgsConstructor(access = AccessLevel.PROTECTED) 요놈이 대신함
//    protected OrderItem(){
//
//    }

    // 생성 메서드
    // 할인이나 쿠폰 때문에 가격이나 재고가 바뀔 경우를 대비해서 orderItem 도 생성 메서드로 해결
    public static OrderItem createOrderItem(Item item , int orderPrice , int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);
        //넘어온 만큼 상품 재고를 깍아줘야함
        item.removeStock(count);
        return orderItem;
    }

    // 비즈니스 로직
    // 주문 취소
    public void cancel(){
        // 주문 수량을 다시 재고수량에 넣어서 복구 시킴
        getItem().addStock(count);
    }

    // 주문 가격 조회
    public int getTotalPrice(){
        return getOrderPrice() * getCount();
    }
}
