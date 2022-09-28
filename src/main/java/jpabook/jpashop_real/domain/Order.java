package jpabook.jpashop_real.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order" , cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY , cascade = CascadeType.ALL)
    @JoinColumn(name = "delibery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    //연관관계 메서드 (양 방향에서 쓰면 좋음)

    public void setMember(Member member){
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    // 생성 메서드//
    // 주문 생성 메서드  orderItem 을 ...점 문법으로 여러개 넘길 수 있음
    // 주문 생성에 대한 복잡한 로직을 그냥 createOrder에서 다 마무리 시킴 나중에 요지보수 할 떄 좋음
    public static Order createOrder(Member member , Delivery delivery , OrderItem... orderItems){
        Order order =new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        // 반복문 돌려서 아이템을 생성
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        // 오더 상태를 처음 상태로 강제로 정함
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    // 비즈니스 로직
    // 주문 취소 메서드
    public void cancel(){
        // 배송이 이미 완료 된 상품은 취소가 불가능
        if(delivery.getStatus() == DeliveryStatus.COMP){
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        // 반복문 돌면서 재고 상태를 원래로 되돌림
        for (OrderItem orderItem : orderItems) {
            // orderItem 에서도 취소를 해야됨 Order에서만 취소하는게 아니라
            orderItem.cancel();
        }
    }

    // 조회 로직
    // 전체 주문 가격 조회 메서드
    public int getTotalPrice() {
        int totalPrice = 0;
        // 반복문 돌면서 orderItem에 있는 가격을 전부 더함
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }


}
