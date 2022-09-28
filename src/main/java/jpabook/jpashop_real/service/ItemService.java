package jpabook.jpashop_real.service;

import jpabook.jpashop_real.domain.item.Item;
import jpabook.jpashop_real.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 데이터 값을 조회만 하기위해 리드온리 트루
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;


    // 변경 감지
    // merge는 모든 값을 다 교체해서 내가 뺴먹은 값도 null로 반환함  그래서 merge 는 사용하지 말도록. 엔티티를 변경할 때는
    // 항상 변경 감지를 사용!
    // 영속성 컨텍스트에서 엔티티를 다시 조회한 후 다시 값을 넣어줌
    @Transactional
    public void updateItem(Long itemid , String name ,int price ,int stockQuantity){
        Item findItem = itemRepository.findOne(itemid);
        findItem.setPrice(price);
        findItem.setName(name);
        findItem.setStockQuantity(stockQuantity);
    }


    // 데이터 값이 바뀌니까 트랜잭션
    @Transactional
    public void saveItem(Item item){
        itemRepository.save(item);
    }

    public List<Item> findItems(){
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId){
        return itemRepository.findOne(itemId);
    }
}
