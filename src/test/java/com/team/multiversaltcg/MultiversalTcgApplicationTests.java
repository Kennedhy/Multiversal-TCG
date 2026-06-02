package com.team.multiversaltcg;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "debug=false",
        "logging.level.root=WARN",
        "logging.level.org.springframework=WARN",
        "logging.level.org.hibernate.SQL=WARN"
})
class MultiversalTcgApplicationTests {

    @Test
    void contextLoads() {
    }

}
