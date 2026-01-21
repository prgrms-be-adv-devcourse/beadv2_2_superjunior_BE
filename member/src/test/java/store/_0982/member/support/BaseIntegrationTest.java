package store._0982.member.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@EmbeddedKafka(kraft = true)
@ContextConfiguration(initializers = PostgreSQLContainerInitializer.class)
public abstract class BaseIntegrationTest {

    @MockitoBean
    protected JavaMailSender javaMailSender;
}
