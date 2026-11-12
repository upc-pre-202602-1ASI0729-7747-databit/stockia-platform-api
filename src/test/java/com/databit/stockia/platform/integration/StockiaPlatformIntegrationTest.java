package com.databit.stockia.platform.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end flows of the API over the demo restaurant (H2 in memory, profile test).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StockiaPlatformIntegrationTest {
    private static final String ADMIN = "admin@databitecorp.com";
    private static final String EMPLOYEE = "empleado@databitecorp.com";
    private static final String PASSWORD = "stockia123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode json(String body) throws Exception {
        return objectMapper.readTree(body);
    }

    private String signIn(String email, String password) throws Exception {
        var result = mockMvc.perform(post("/api/v1/authentication/sign-in").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty()).andReturn();
        return "Bearer " + json(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long recipeId(String token, String dishName) throws Exception {
        var body = mockMvc.perform(get("/api/v1/recipes").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        for (var recipe : json(body)) if (recipe.get("dishName").asText().equals(dishName)) return recipe.get("id").asLong();
        throw new AssertionError("Recipe not found: " + dishName);
    }

    @Test
    void healthAndPlansArePublic() throws Exception {
        mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/api/v1/plans").header(HttpHeaders.ACCEPT_LANGUAGE, "es"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name").value("Esencial"))
                .andExpect(jsonPath("$[1].highlighted").value(true))
                .andExpect(jsonPath("$[1].features[0]").value("Insumos ilimitados"));
    }

    @Test
    void protectedEndpointsNeedAToken() throws Exception {
        mockMvc.perform(get("/api/v1/inventory-items"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void wrongPasswordsAreCountedAndMessagesAreLocalized() throws Exception {
        mockMvc.perform(post("/api/v1/authentication/sign-in").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@databitecorp.com\",\"password\":\"x\"}").header(HttpHeaders.ACCEPT_LANGUAGE, "es-419"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("InvalidCredentials"))
                .andExpect(jsonPath("$.detail").value("Correo o contraseña incorrectos."));
        mockMvc.perform(post("/api/v1/authentication/sign-in").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"wrong-password1\"}".formatted(EMPLOYEE)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Incorrect email or password."))
                .andExpect(jsonPath("$.details.maxAttempts").value(5))
                .andExpect(jsonPath("$.details.remainingAttempts").value(lessThan(5)));
        signIn(EMPLOYEE, PASSWORD);
    }

    @Test
    void employeesCannotManageTheTeam() throws Exception {
        var token = signIn(EMPLOYEE, PASSWORD);
        mockMvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    @Test
    void theDashboardSummarizesTheDemoRestaurant() throws Exception {
        var token = signIn(ADMIN, PASSWORD);
        mockMvc.perform(get("/api/v1/dashboard/summary").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registeredSupplies").value(10))
                .andExpect(jsonPath("$.expiredSupplies").value(2))
                .andExpect(jsonPath("$.confirmedSales").value(greaterThan(100)))
                .andExpect(jsonPath("$.pendingAlerts").value(greaterThan(0)));
        mockMvc.perform(get("/api/v1/dashboard/weekly-savings").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estimatedSavings").isNumber());
        mockMvc.perform(get("/api/v1/alerts").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].message").isNotEmpty());
    }

    @Test
    void theDemandForecastUsesTheOnnxModel() throws Exception {
        var token = signIn(ADMIN, PASSWORD);
        mockMvc.perform(get("/api/v1/demand-forecasts/model").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ready").value(true));
        mockMvc.perform(post("/api/v1/demand-forecasts").header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"horizonDays\":7}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.method").value("MACHINE_LEARNING"))
                .andExpect(jsonPath("$.weatherSource").value("climatology"))
                .andExpect(jsonPath("$.dataPoints", hasSize(7)));
        mockMvc.perform(post("/api/v1/demand-forecasts").header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"horizonDays\":9}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("InvalidHorizon"));
        mockMvc.perform(get("/api/v1/recommendations").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].message").isNotEmpty());
    }

    @Test
    void aSaleIsRejectedWhenASupplyIsExpiredAndRecordedOtherwise() throws Exception {
        var token = signIn(ADMIN, PASSWORD);
        var alfredo = recipeId(token, "Fettuccine Alfredo");
        mockMvc.perform(post("/api/v1/sales").header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipeId\":%d,\"quantity\":1}".formatted(alfredo)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("CannotSell"))
                .andExpect(jsonPath("$.details.dish").value("Fettuccine Alfredo"))
                .andExpect(jsonPath("$.details.shortages[0].reason").value("EXPIRED"));

        var salad = recipeId(token, "Chicken Caesar Salad");
        var body = mockMvc.perform(post("/api/v1/sales").header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipeId\":%d,\"quantity\":2,\"channel\":\"MANUAL\"}".formatted(salad)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.channel").value("MANUAL"))
                .andExpect(jsonPath("$.lineItems[0].quantity").value(2))
                .andReturn().getResponse().getContentAsString();
        var saleId = json(body).get("id").asLong();
        mockMvc.perform(post("/api/v1/sales/%d/void".formatted(saleId)).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("VOIDED"));
        mockMvc.perform(post("/api/v1/sales/%d/void".formatted(saleId)).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.title").value("AlreadyVoided"));
    }

    @Test
    void invalidDataAnswersWithTheFieldErrors() throws Exception {
        var token = signIn(ADMIN, PASSWORD);
        mockMvc.perform(post("/api/v1/recipes").header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dishName\":\"\",\"ingredients\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.dishName").isArray())
                .andExpect(jsonPath("$.errors.ingredients").isArray());
    }

    @Test
    void aNewRestaurantSignsUpAndRecoversItsPassword() throws Exception {
        var email = "owner-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        var registration = mockMvc.perform(post("/api/v1/restaurant-registrations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana Torres\",\"restaurantName\":\"La Picanteria\",\"email\":\"%s\",\"password\":\"secret123\"}"
                                .formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.restaurantName").value("La Picanteria"))
                .andReturn().getResponse().getContentAsString();
        var token = "Bearer " + json(registration).get("token").asText();
        mockMvc.perform(get("/api/v1/subscriptions/current").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.paymentMethod").value("NONE"));
        mockMvc.perform(get("/api/v1/inventory-items").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(post("/api/v1/restaurant-registrations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana Torres\",\"restaurantName\":\"Otro\",\"email\":\"%s\",\"password\":\"secret123\"}".formatted(email)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.title").value("EmailAlreadyTaken"));

        var request = mockMvc.perform(post("/api/v1/authentication/password-reset-requests").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\"}".formatted(email)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.sentTo").value(startsWith("o")))
                .andExpect(jsonPath("$.simulatedEmail.code").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        var code = json(request).get("simulatedEmail").get("code").asText();
        mockMvc.perform(post("/api/v1/authentication/password-resets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"code\":\"%s\",\"newPassword\":\"newSecret456\"}".formatted(email, code)))
                .andExpect(status().isNoContent());
        assertThat(signIn(email, "newSecret456")).startsWith("Bearer ");
    }
}
