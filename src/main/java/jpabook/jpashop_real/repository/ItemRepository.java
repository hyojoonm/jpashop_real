package jpabook.jpashop_real.repository;

import jpabook.jpashop_real.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    //상품 저장 (save 메서드를 사용하면 새로운 객체를 생성해서 등록.) 업데이트라 생각 하면 됨 (비슷함)
    public void save(Item item){
        // 아이템이 저장되어 있지 않으면 아이디 값이 없기 떄문에 id값
        if(item.getId() == null){
            em.persist(item);
        }else{
            //
            em.merge(item);
        }
    }
    // 하나 조회는 그냥 찾음
    public Item findOne(Long id){
        return em.find(Item.class,id);
    }
    // 여러개 조회는 쿼리를 날려서 리스트로 저장
    public List<Item> findAll(){
        return em.createQuery("select i from Item i",Item.class)
                .getResultList();
    }
}
