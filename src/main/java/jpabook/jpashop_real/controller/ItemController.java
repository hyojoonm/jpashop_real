package jpabook.jpashop_real.controller;

import jpabook.jpashop_real.domain.item.Book;
import jpabook.jpashop_real.domain.item.Item;
import jpabook.jpashop_real.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public  String createForm(Model model){
        model.addAttribute("form",new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form){

        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        book.setStockQuantity(form.getStockQuantity());

        itemService.saveItem(book);
        return "redirect:/";
    }

    @GetMapping("/items")
    public String list(Model model){
        List<Item> items = itemService.findItems();
        model.addAttribute("items",items);
        return "items/itemList";
    }

    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId , Model model){
        Book item = (Book) itemService.findOne(itemId);

        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form",form);
        return "items/updateItemForm";
    }
    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId ,@ModelAttribute("form") BookForm form) {
        // jpa에 들어갔다가 한번 나온 객체를 준영속 상태 객체
        // jpa가 식별할 수 있는 id 값을 이미 가지고 있음 jpa 가 관리를 하지 않음 DB에 업데이트가 안됨
        // 컨트롤러에서 데이터 하나씩 set 하지말고 서비스클래스 가서 메서드를 하나 만들어서 간편하게 업데이트!
//        Book book = new Book();
//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());

        itemService.updateItem(itemId ,form.getName() , form.getPrice(),form.getStockQuantity());
        return "redirect:/items";

    }
}
