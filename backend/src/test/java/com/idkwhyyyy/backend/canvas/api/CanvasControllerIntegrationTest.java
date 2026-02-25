package com.idkwhyyyy.backend.canvas.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idkwhyyyy.backend.canvas.api.dto.CreateCanvasRequest;
import com.idkwhyyyy.backend.canvas.api.dto.RenameCanvasRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CanvasControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private String authToken;

  @BeforeEach
  void setUp() throws Exception {
    authToken = registerAndGetToken();
  }

  @Test
  void shouldCreateAndFetchCanvasWithInitialGraph() throws Exception {
    MvcResult createResult =
        mockMvc
            .perform(
                withUser(post("/api/v1/canvases"))
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new CreateCanvasRequest("Arena"))))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", containsString("/api/v1/canvases/")))
            .andExpect(jsonPath("$.name").value("Arena"))
            .andExpect(jsonPath("$.nextNodeIndex").value(4))
            .andExpect(jsonPath("$.graph.nodes[*].id", hasItems("start", "validate", "done")))
            .andExpect(
                jsonPath("$.graph.edges[*].id", hasItems("start-validate", "validate-done")))
            .andReturn();

    String canvasId = readId(createResult);

    mockMvc
        .perform(withUser(get("/api/v1/canvases/{canvasId}", canvasId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(canvasId))
        .andExpect(jsonPath("$.graph.nodes[*].id", hasItems("start", "validate", "done")))
        .andExpect(jsonPath("$.graph.edges[*].id", hasItems("start-validate", "validate-done")));
  }

  @Test
  void shouldRenameListAndDeleteCanvas() throws Exception {
    MvcResult createResult =
        mockMvc
            .perform(
                withUser(post("/api/v1/canvases"))
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new CreateCanvasRequest("   "))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Canvas 1"))
            .andReturn();

    String canvasId = readId(createResult);

    mockMvc
        .perform(
            withUser(patch("/api/v1/canvases/{canvasId}", canvasId))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RenameCanvasRequest("Dojo"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Dojo"));

    mockMvc
        .perform(withUser(get("/api/v1/canvases")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(canvasId))
        .andExpect(jsonPath("$[0].name").value("Dojo"));

    mockMvc.perform(withUser(delete("/api/v1/canvases/{canvasId}", canvasId))).andExpect(status().isNoContent());

    mockMvc
        .perform(withUser(get("/api/v1/canvases/{canvasId}", canvasId)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("canvas_not_found"));
  }

  @Test
  void shouldApplyNodeAndEdgeChangesAndValidateInvalidEdge() throws Exception {
    MvcResult createResult =
        mockMvc
            .perform(
                withUser(post("/api/v1/canvases"))
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new CreateCanvasRequest("Flow"))))
            .andExpect(status().isCreated())
            .andReturn();

    String canvasId = readId(createResult);

    mockMvc
        .perform(
            withUser(post("/api/v1/canvases/{canvasId}/nodes", canvasId))
                .contentType(APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nextNodeIndex").value(5))
        .andExpect(jsonPath("$.graph.nodes[*].id", hasItems("step-4")))
        .andExpect(jsonPath("$.graph.edges[*].id", hasItems("done-step-4")));

    mockMvc
        .perform(
            withUser(post("/api/v1/canvases/{canvasId}/edges", canvasId))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "source": "start",
                      "target": "done"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.graph.edges[*].id", hasItems("start-done")));

    mockMvc
        .perform(
            withUser(patch("/api/v1/canvases/{canvasId}/nodes", canvasId))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "changes": [
                        {
                          "type": "position",
                          "id": "step-4",
                          "position": {
                            "x": 900,
                            "y": 500
                          }
                        }
                      ]
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.graph.nodes[?(@.id=='step-4')].position.x").value(900.0))
        .andExpect(jsonPath("$.graph.nodes[?(@.id=='step-4')].position.y").value(500.0));

    mockMvc
        .perform(
            withUser(patch("/api/v1/canvases/{canvasId}/edges", canvasId))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "changes": [
                        {
                          "type": "remove",
                          "id": "start-done"
                        }
                      ]
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.graph.edges[*].id", not(hasItems("start-done"))));

    mockMvc
        .perform(
            withUser(patch("/api/v1/canvases/{canvasId}/nodes", canvasId))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "changes": [
                        {
                          "type": "remove",
                          "id": "step-4"
                        }
                      ]
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.graph.nodes[*].id", not(hasItems("step-4"))))
        .andExpect(jsonPath("$.graph.edges[*].id", not(hasItems("done-step-4"))));

    mockMvc
        .perform(
            withUser(post("/api/v1/canvases/{canvasId}/edges", canvasId))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "source": "missing",
                      "target": "done"
                    }
                    """))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.title").value("invalid_edge"));
  }

  private MockHttpServletRequestBuilder withUser(MockHttpServletRequestBuilder builder) {
    return builder.header("Authorization", "Bearer " + authToken);
  }

  private String registerAndGetToken() throws Exception {
    String email = "tester-" + UUID.randomUUID() + "@local.test";
    String payload =
        objectMapper.writeValueAsString(
            java.util.Map.of(
                "email", email,
                "password", "secret123"));

    MvcResult result =
        mockMvc
            .perform(post("/api/v1/auth/register").contentType(APPLICATION_JSON).content(payload))
            .andExpect(status().isCreated())
            .andReturn();

    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.get("token").asText();
  }

  private String readId(MvcResult result) throws Exception {
    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.get("id").asText();
  }
}
