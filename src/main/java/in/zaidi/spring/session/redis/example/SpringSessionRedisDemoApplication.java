package in.zaidi.spring.session.redis.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class SpringSessionRedisDemoApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SpringSessionRedisDemoApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SpringSessionRedisDemoApplication.class);
    }

}
