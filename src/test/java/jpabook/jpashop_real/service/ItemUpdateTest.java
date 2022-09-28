package jpabook.jpashop_real.service;

import jpabook.jpashop_real.domain.item.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ItemUpdateTest {

    @Autowired
    EntityManager em ;

    @Test
    public void updateTest() throws Exception{

        Book book = em.find(Book.class, 1L);

        //트랜잭션 안에서
        book.setName("asdasf");

        // 트랜잭션 커밋하면 JPA에서 변경된 부분을 찾아서 알아서 업데이트 쿼리 날려서 DB에 반영 = dirty checking(변경 감지)
        // 플러쉬 할 때 더티체킹이 일어남
    }
}
