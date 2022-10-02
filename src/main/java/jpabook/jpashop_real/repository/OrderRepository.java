package jpabook.jpashop_real.repository;

import jpabook.jpashop_real.domain.Order;
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
    public List<Order> finAllWithMemberDelivery() {
        // Order 를 조회하면서 SQL 입장에서는 조인이면서 셀렉트 절에서 멤버와 딜리버리를 조인한 다음에 가져옴
        // LAZY 도 무시하고 그냥 다 가져옴 = 이게 바로 패치 조인  패치 조인은 실무에서도 자주 쓰니 강좌 다시 복습해서 100% 이해하도록.
        return em.createQuery(
                "select o from Order o " +
                        "join fetch o.member m" +
                        " join fetch o.delivery d" ,Order.class)
                .getResultList();
    }


}
