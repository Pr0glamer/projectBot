package nix.projectbot;

import nix.projectbot.controllers.BotController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class ProjectBotApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BotController botController;

    @Test
    void contextLoads() {
    }
    @Test
    public void test() throws Exception {
        this.mockMvc.perform(get("/botinfo")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Index</title>")));
    }

}
