package com.nibado.example.springasync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.nibado.example.springasync.domain.ApiRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AsyncApplicationIntegrationTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String[] ROUTES = {
            "/weather/utrecht",
            "/weather/london",
            "/weather/sydney",
            "/weather/amsterdam",
            "/geo/utrecht",
            "/geo/london",
            "/geo/sydney",
            "/geo/amsterdam",
    };

    @Autowired
    private WebApplicationContext wac;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    private String baseUrl;
    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception {
        mockMvc = webAppContextSetup(this.wac).build();
        baseUrl = "http://localhost:" + wireMockRule.port();

        stubServices();
    }

    private void stubServices() throws Exception {
        //Total delay = 8 * 250 = 2000ms.
        stubService(ROUTES[0], new WeatherResponse(25.0f, "Sunny", 10));
        stubService(ROUTES[1], new WeatherResponse(15.0f, "Cloudy", 10));
        stubService(ROUTES[2], new WeatherResponse(30.0f, "Sunny", 0));
        stubService(ROUTES[3], new WeatherResponse(10.0f, "Overcast", 0));

        stubService(ROUTES[4], new GeoResponse(52.087515, 5.119569));
        stubService(ROUTES[5], new GeoResponse(51.5285578,-0.2420237));
        stubService(ROUTES[6], new GeoResponse(-33.873061, 151.092616));
        stubService(ROUTES[7], new GeoResponse(52.372333, 4.898421));
    }

    private void stubService(String route, Object value) throws Exception {
        stubFor(get(urlEqualTo(route))
                .willReturn(aResponse()
                        .withFixedDelay(250)
                        .withHeader("Content-Type", "application/json")
                        .withBody(MAPPER.writeValueAsBytes(value))));
    }

    @Test
    public void happyFlow() throws Exception {
        ApiRequest request = new ApiRequest(Arrays.stream(ROUTES).map(s -> baseUrl + s).collect(Collectors.toList()));

        MvcResult resultActions = mockMvc.perform(post("/aggregate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(resultActions))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.responses[0].status").value(200))
                .andExpect(jsonPath("$.responses[0].duration").isNumber())
                .andExpect(jsonPath("$.responses[0].body.celsius").value(25.0))
                .andExpect(jsonPath("$.responses[1].body.celsius").value(15.0))
                .andExpect(jsonPath("$.responses[2].body.celsius").value(30.0))
                .andExpect(jsonPath("$.responses[3].body.celsius").value(10.0))
                .andExpect(jsonPath("$.responses[4].body.lat").value(52.087515))
                .andExpect(jsonPath("$.responses[5].body.lat").value(51.5285578))
                .andExpect(jsonPath("$.responses[6].body.lat").value(-33.873061))
                .andExpect(jsonPath("$.responses[7].body.lat").value(52.372333))
                .andExpect(jsonPath("$.duration", Matchers.lessThan(750))); //Should be around 500ms.
    }

    @Data
    @AllArgsConstructor
    private static class WeatherResponse {
        private float celsius;
        private float fahrenheit;
        private String clouds;
        private int rain;

        public WeatherResponse(float celsius, String clouds, int rain) {
            this.celsius = celsius;
            this.fahrenheit = celsius * 9.0f/5.0f + 32.0f;
            this.clouds = clouds;
            this.rain = rain;
        }
    }

    @Data
    @AllArgsConstructor
    public static class GeoResponse {
        private double lat;
        private double lon;
    }
}