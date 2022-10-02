package jpabook.jpashop_real.api;

import jpabook.jpashop_real.domain.Address;
import jpabook.jpashop_real.domain.Order;
import jpabook.jpashop_real.domain.OrderStatus;
import jpabook.jpashop_real.repository.OrderRepository;
import jpabook.jpashop_real.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    // DTO로 변환해서 출력
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        /* 주문 기록을 가져와서 스트림으로 돌아감     ORDER 2개
        이게 그 유명한 N + 1 문제
        N + 1 -> 1 + N   1= 첫번째 쿼리 오더를 호출  N은 = 오더 주문을 호출 (현재 예제에서 주문2개 입력함) 2개
        N + 1 -> 1 + N(2) = 1 + 회원 N + 배송 N    N = 2 이니까 쿼리가 5개 실행
        그렇다고 LAZY 말고 EAGER 로 바꾼다? 안돼 안된다면 하지 좀 마 모르겠으면 외워 그냥 !! EAGER 는 영한이형도  예측이 안됨
         */
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        // 결과가 2개면 루프를 2번 돔
        // map 은 o DTO 로 바꿈 그걸 다시 또 리스트로 변환
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());

        return result;
    }

    // V2 와 V3는 결과적으로 똑같다 하지만 쿼리에 수가 다르다 ( 이것이 최적화 )
    // 패치조인 적용
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.finAllWithMemberDelivery();

        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }





    // 내가 출력하고 싶은 데이터를 넣음
    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;
        // Dto 가 엔티티를 받는건 상관없음 별 중요하지 않는 곳에서 의존하는 것이기 때문에
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화 영속성 컨테스트에 찾아보는데 네임이 없으면 DB 쿼리를 날림
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();  // LAZY 초기화 영속성 컨테스트에 찾아보는데 네임이 없으면 DB 쿼리를 날림
        }
    }
    /*
    여기까지 쿼리를 보면 오더 쿼리를 날리고 멤버랑 딜리버리 쿼리 한번날리고 두번쨰 주문을 조회하기 위해서 또 다시 멤버와 딜리버리 쿼리를 날림
    쿼리를 5번 날린 이유는
    ORDER -> SQL 1번 -> 결과 주문수 2개 2개면 위에 스트림 루프를 2번 돔  ORDER 엔티티에서 멤버와 딜리버리에 LAZY 지연 로딩 설정해서 계속 초기화 됨

    */
}
