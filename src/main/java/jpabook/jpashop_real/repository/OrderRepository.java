package jpabook.jpashop_real.repository;


import jpabook.jpashop_real.domain.Order;
import jpabook.jpashop_real.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class,id);
    }

   public List<Order> findAllByString (OrderSearch orderSearch){
       //language=JPAQL
       String jpql = "select o From Order o join o.member m";
       boolean isFirstCondition = true;
       //주문 상태 검색
       if (orderSearch.getOrderStatus() != null) {
           if (isFirstCondition) {
               jpql += " where";
               isFirstCondition = false;
           } else {
               jpql += " and";
           }
           jpql += " o.status = :status";
       }
       //회원 이름 검색
       if (StringUtils.hasText(orderSearch.getMemberName())) {
           if (isFirstCondition) {
               jpql += " where";
               isFirstCondition = false;
           } else {
               jpql += " and";
           }
           jpql += " m.name like :name";
       } TypedQuery<Order> query = em.createQuery(jpql, Order.class)
               .setMaxResults(1000); //최대 1000건
       if (orderSearch.getOrderStatus() != null) {
           query = query.setParameter("status", orderSearch.getOrderStatus());
       }
       if (StringUtils.hasText(orderSearch.getMemberName())) {
           query = query.setParameter("name", orderSearch.getMemberName());
       }
       return query.getResultList();
   }

    // 패치 조인 적용 예제 메서드
    public List<Order> findAllWithMemberDelivery() {
        // Order 를 조회하면서 SQL 입장에서는 조인이면서 셀렉트 절에서 멤버와 딜리버리를 조인한 다음에 가져옴
        // LAZY 도 무시하고 그냥 다 가져옴 = 이게 바로 패치 조인  패치 조인은 실무에서도 자주 쓰니 강좌 다시 복습해서 100% 이해하도록.
        return em.createQuery(
                "select o from Order o " +
                        "join fetch o.member m" +
                        " join fetch o.delivery d" ,Order.class)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset , int limit) {
        return em.createQuery(
                        "select o from Order o " +
                                "join fetch o.member m" +
                                " join fetch o.delivery d" , jpabook.jpashop_real.domain.Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
    // ToOne 관계는 패치 조인 안해도 하이버네이트 배치 패치 사이즈가 최적화 해주지만 그래도 네트워크를 많이 타기 떄문에 조인 해주자!


    /*
    여기서 단점은 order 는 2개 order item 은 4개다 이것을 조인을 하면 order 도 4개가 된다.
    조인을 하면 쿼리에서는 그냥 한줄씩 쭉쭉 출력하는거라 오더는 2개지만 오더 아이템이 4개라 A 오더에 아이템 2개 출력 B 오더에 아이템 2개 출력
     */
    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
                .setFirstResult(1)
                .setMaxResults(100)
                .getResultList();
    }
    /* DB에서 중복제거 distinct 는 데이터가 다 똑같아야 제거 가능 그래서 DB에서는 중복제거가 안됨
     하지만 jpa에서 자체적으로 order를 가지고 올떄 orderId 값이 같으면 중복을 제거 해준다 JPA는 신!

    우리는 오더 기준으로 페이징을 하고 싶었는데 DB에서는 order 보다 orderItem에 개수가 더 많아서 orderItem 기준으로 페이징 하게 됨

     단점: 페이징 불가능
     1대다 하는 순간 페이징 쿼리가 안나감
     페이징: .setFirstResult , setMaxResults 등
     일대다 매핑에서 페이징이 불가능. 뻥튀기 데이터 떄문에 DB상으로 페이징이 불가능
      하이버네이트는 경고 로그를 남기면서 모든 데이터를 DB에서 읽어오고, 메모리에서 페이징 해버린다(매우 위험하다)

     컬렉션 패치 조인은 하나만 사용가능 , 일대다 도 복잡한데 일대다 대 다 면 데이터가 뻥튀기돼서 DB에서 정리도 못하고 그냥 막 데이터가 수도 없이
     늘어나서 DB도 죽고 나도 죽고 다죽음

     */
}
