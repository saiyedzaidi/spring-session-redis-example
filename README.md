# spring-session-redis-example
Example project for Redis based spring session


Designed as a war project, generate war using mvn package and install in a servlet container.

To generate an executable jar:
1. change the maven project type from war to jar
2. remove the exclusion of spring-boot-starter-tomcat< by deleting away this dependency or just deleting the provided scope.
 ```XML
 <dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-tomcat</artifactId>
			<scope>provided</scope>
</dependency>
 ```
    
3. Comment out the method 
```Java
    // The method SpringSessionRedisExampleApplication.configure
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SpringSessionRedisExampleApplication.class);
    }
```
