package jpabook.jpashop_real.api;

import jpabook.jpashop_real.domain.Address;
import jpabook.jpashop_real.domain.Order;
import jpabook.jpashop_real.domain.OrderStatus;
import jpabook.jpashop_real.repository.OrderRepository;
import jpabook.jpashop_real.repository.OrderSearch;
import jpabook.jpashop_real.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop_real.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

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
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }
    /*
    여기까지 쿼리를 보면 오더 쿼리를 날리고 멤버랑 딜리버리 쿼리 한번날리고 두번쨰 주문을 조회하기 위해서 또 다시 멤버와 딜리버리 쿼리를 날림
    쿼리를 5번 날린 이유는
    ORDER -> SQL 1번 -> 결과 주문수 2개 2개면 위에 스트림 루프를 2번 돔  ORDER 엔티티에서 멤버와 딜리버리에 LAZY 지연 로딩 설정해서 계속 초기화 됨
    */

    /*
        V3 와 V4는 우열을 가리기 힘듬   V4가 더 셀렉트 하는 것이 적어서 조금 더 성능 최적화 됨
        V3는 오더에서 패치조인으로 원하는 것만 셀렉트해서 가져 온것 (외부의 모습을 건드리지 않음 내부에 원하는 것만
        패치 조인으로 가져옴)   DTO 재사용 가능!  여러 방면에서 활용 가능
        V4는 실제 SQL하듯이 JPQL로 그냥 짜서 가져옴 그래서 DTO가 재사용성이 없음  이 DTO는 여기서만 사용 가능

     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }

    /*
        3번과 4번 중에서 골라야 함  코드 상으로 3번이 더 좋지만 성능상 4번이 좋음  (근데 성능차이 그렇게 심하지 않음...)
        실제로 성능차이는 셀렉트가 아닌 웨어나 조인 ,조건 등이 성능을 먹어버림;
        엔티티를 DTO로 변환하거나, DTO로 바로 조회하는 두가지 방법은 각각 장단점이 있다. 둘중 상황에
        따라서 더 나은 방법을 선택하면 된다. 엔티티로 조회하면 리포지토리 재사용성도 좋고, 개발도 단순해진다.
        따라서 권장하는 방법은 다음과 같다.

쿼리 방식 선택 권장 순서
1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다.
3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접
사용한다.

     */


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

}
