package com.nibado.example.springasync;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SimpleControllerIntegrationTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception {
        mockMvc = webAppContextSetup(this.wac).build();
    }

    @Test
    public void testBasic() throws Exception {
        testSync("/time/basic");
    }

    @Test
    public void testResponseEntity() throws Exception {
        testSync("/time/re");
    }

    @Test
    public void testCallable() throws Exception {
        testAsync("/time/callable");
    }

    @Test
    public void testDeferred() throws Exception {
        testAsync("/time/deferred");
    }


    private void testSync(String route) throws Exception {
        mockMvc.perform(get(route))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.time").isString());
    }

    private void testAsync(String route) throws Exception {
        MvcResult resultActions = mockMvc.perform(get(route))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(resultActions))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.time").isString());
    }
}