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
