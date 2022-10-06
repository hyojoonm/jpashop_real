package jpabook.jpashop_real;


import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class JpashopRealApplication {

	public static void main(String[] args) {

		SpringApplication.run(JpashopRealApplication.class, args);
	}

	@Bean
	Hibernate5Module hibernate5Moudle (){
		Hibernate5Module hibernate5Module = new Hibernate5Module();
		return hibernate5Module;
	}

}
