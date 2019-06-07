package in.zaidi.spring.session.redis.example.config;

import java.util.Arrays;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.session.data.redis.RedisFlushMode;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.session.web.http.CookieHttpSessionStrategy;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import redis.clients.jedis.JedisPoolConfig;

@EnableRedisHttpSession
@PropertySource(
        value = { "classpath:application.properties" })
public class SpringSessionConfig {

    /** Maximum inactive interval for Redis session */
    @Value("${redis.session.timeout:}")
    private Integer maxInactiveIntervalInSeconds;

    /** Host name of Redis server */
    @Value("${redis.server.hostname:}")
    private String hostName;

    /** Port number of Redis server */
    @Value("${redis.server.port:}")
    private Integer port;

    /** If use connection pool for Redis connection */
    @Value("${redis.use.connection.pool:}")
    private Boolean useConnectionPool;

    /** Connection timeout period with Redis server */
    @Value("${redis.connection.timeout:}")
    private Integer connectionTimeout;

    /** Maximum number of connections with Redis server */
    @Value("${redis.max.connection:}")
    private Integer maxTotalConnections;

    /** Maximun number of idle connections with Redis server */
    @Value("${redis.max.idle.connection:}")
    private Integer maxIdleConnections;

    /** Minimum number of idle connections with Redis server */
    @Value("${redis.min.idle.connection:}")
    private Integer minIdleConnections;

    /** Redis session cookie Name */
    @Value("${redis.session.cookie.name:}")
    private String httpSessionIdName;

    /** Redis cluster nodes */
    @Value("${redis.cluster.nodes:}")
    private String redisClusterNodes;

    /** Logger to perform logging activities */
    private static final Logger LOG = LoggerFactory.getLogger(SpringSessionConfig.class);

    /**
     * This method returns the object of JedisPoolConfig which configures connection pool used to connect with Redis Server.
     * 
     * @return jedisPoolConfig the Redis connection pool configuration object
     */
    @Bean
    public JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotalConnections);
        jedisPoolConfig.setMaxIdle(maxIdleConnections);
        jedisPoolConfig.setMinIdle(minIdleConnections);
        return jedisPoolConfig;
    }

    /**
     * This method returns the object of JedisConnectionFactory which is required to get a connection to Redis
     * 
     * @return JedisConnectionFactory the jedis connection factory
     */
    @Bean
    @Primary
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        LOG.info("Redis cluster Node::{}", redisClusterNodes);
        JedisConnectionFactory jedisConnectionFactory;
        if (hostName != null && !hostName.isEmpty()) {
            System.out.println("Using hostname for standlone Redis connection");
            jedisConnectionFactory = new JedisConnectionFactory();
            jedisConnectionFactory.setHostName(hostName);
            jedisConnectionFactory.setPort(port);
            jedisConnectionFactory.setUsePool(false);
        } else {
            System.out.println("Using Nodes for clustered Redis connection");
            HashSet<RedisNode> nodes = new HashSet<>();
            String[] nodeArray = redisClusterNodes.split("\\|");
            for (String ipPort : nodeArray) {
                String[] ipPortPair = ipPort.split(":");
                RedisNode redisNode = new RedisNode(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim()));
                nodes.add(redisNode);
            }
            redisClusterConfiguration.setClusterNodes(nodes);
            jedisConnectionFactory = new JedisConnectionFactory(redisClusterConfiguration, getJedisPoolConfig());

        }
        jedisConnectionFactory.setTimeout(connectionTimeout);
        jedisConnectionFactory.setUsePool(useConnectionPool);
        LOG.info("JedisConnectionFactory object created::{}", jedisConnectionFactory);
        System.out.println("JedisConnectionFactory object created::{}" + jedisConnectionFactory);
        return jedisConnectionFactory;
    }

    /**
     * This method gives ConfigureRedisAction object which specifies if Redis Keyspace events for Generic commands and Expired events are
     * enabled. Default behaviour of Redis events action does not work if the Redis instance (e.g. AWS Redis Elasticache) has been properly
     * secured and hence causes run time exceptions. To avoid this a bean of type ConfigureRedisAction.NO_OP is exposed which is a do
     * nothing implementation of ConfigureRedisAction interface.
     * 
     * @return configureRedisAction the configureRedisAction object
     */
    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

    /**
     * This method creates and returns the RedisOperationsSessionRepository object. This object is used to perform various operations on
     * Redis session like get, save, delete etc.
     * 
     * @param jedisConnectionFactory
     *            the jedis connection factory object
     * @param applicationEventPublisher
     *            the ApplicationEventPublisher parameter
     * @return sessionRepository the RedisOperationsSessionRepository object
     */
    @Bean
    public RedisOperationsSessionRepository sessionRepository(JedisConnectionFactory jedisConnectionFactory,
            ApplicationEventPublisher applicationEventPublisher) {
        RedisOperationsSessionRepository sessionRepository = new RedisOperationsSessionRepository(jedisConnectionFactory);
        sessionRepository.setDefaultMaxInactiveInterval(maxInactiveIntervalInSeconds);
        sessionRepository.setApplicationEventPublisher(applicationEventPublisher);
        LOG.info("RedisOperationsSessionRepository object created::{}", sessionRepository);
        return sessionRepository;
    }

    /**
     * This method returns CookieHttpSessionStrategy object that uses a cookie to obtain the session from.
     * 
     * @return httpSessionStrategy the CookieHttpSessionStrategy object
     */
    @Bean
    public CookieHttpSessionStrategy httpSessionStrategy() {
        CookieHttpSessionStrategy httpSessionStrategy = new CookieHttpSessionStrategy();
        httpSessionStrategy.setCookieSerializer(cookieSerializer());
        httpSessionStrategy.setSessionAliasParamName(null);
        return httpSessionStrategy;
    }

    /**
     * This method creates Http Session configurations for Redis. One of the configurations is, when to write data into Redis session.
     * RedisFlushMode enum provides two constants IMMEDIATE and ON_SAVE. As the name suggests IMMEDIATE writes data to Redis session as soon
     * data in session is added or changed. ON_SAVE writes data writes to Redis only when
     * SessionRepository.save(org.springframework.session.Session) is invoked.
     * 
     * @return redisHttpSessionConfiguration the Redis Http session configuration object
     */

    @Bean
    public RedisHttpSessionConfiguration redisHttpSessionConfiguration() {
        RedisHttpSessionConfiguration redisHttpSessionConfiguration = new RedisHttpSessionConfiguration();
        redisHttpSessionConfiguration.setHttpSessionStrategy(httpSessionStrategy());
        redisHttpSessionConfiguration.sessionRedisTemplate(jedisConnectionFactory());
        redisHttpSessionConfiguration.setRedisFlushMode(RedisFlushMode.IMMEDIATE);
        return redisHttpSessionConfiguration;
    }

    /**
     * This method is used to set various cookie serialization parameters e.g. cookie name,cookie path, jvmroute name, cookie age etc.
     * 
     * @return CookieSerializer the cookie serializer object
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName(httpSessionIdName);
        return serializer;
    }

    /**
     * This method creates and returns Redis message listener object. This object listens to Session Deletion and Session Expiration events
     * received from Redis server whenever session is deleted or created in Redis
     * 
     * @param jedisConnectionFactory
     *            the JedisConnectionFactory object
     * @param sessionRepository
     *            the RedisOperationsSessionRepository object
     * @return container the RedisMessageListenerContainer object
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(JedisConnectionFactory jedisConnectionFactory,
            RedisOperationsSessionRepository sessionRepository) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(jedisConnectionFactory);
        container.addMessageListener(sessionRepository,
                Arrays.asList(new PatternTopic("__keyevent@*:del"), new PatternTopic("__keyevent@*:expired")));
        container.addMessageListener(sessionRepository,
                Arrays.asList(new PatternTopic(sessionRepository.getSessionCreatedChannelPrefix() + "*")));
        LOG.info("RedisMessageListenerContainer object created::{}", container);
        return container;
    }
}
