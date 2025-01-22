package com.moh4lych.springdi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moh4lych.springdi.model.BeerDTO;
import com.moh4lych.springdi.model.BeerStyle;
import com.moh4lych.springdi.services.BeerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(BeerController.class)
class BeerControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BeerService beerService;

    @Captor
    ArgumentCaptor<UUID> uuidCaptor;

    @Captor
    ArgumentCaptor<BeerDTO> beerCaptor;

    private BeerDTO testBeer;

    @BeforeEach
    void setUp() {
        testBeer = BeerDTO.builder()
                .id(UUID.randomUUID())
                .beerName("My Beer Brand")
                .beerStyle(BeerStyle.PALE_ALE)
                .upc("beerbeer")
                .price(new BigDecimal("12.99"))
                .version(1)
                .quantityOnHand(29)
                .build();
    }

    @Test
    void testGetBeerById() throws Exception {
        given(beerService.getBeerById(any(UUID.class))).willReturn(Optional.of(testBeer));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer/" + UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.beerName", is(testBeer.getBeerName())));
    }

    @Test
    void testCreateBeer() throws Exception {
        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(testBeer);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/beer/")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBeer)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "/api/v1/beer/" + testBeer.getId().toString()));
    }

    @Test
    void testPutBeer() throws Exception {
        given(beerService.updateBeerById(any(UUID.class), any(BeerDTO.class))).willReturn(Optional.of(testBeer));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/beer/" + UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBeer)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        verify(beerService).updateBeerById(any(UUID.class), any(BeerDTO.class));
    }

    @Test
    void testDeleteBeer() throws Exception {
        given(beerService.deleteBeerById(any(UUID.class))).willReturn(Optional.of(testBeer));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/beer/" + testBeer.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        verify(beerService).deleteBeerById(uuidCaptor.capture());

        assertThat(testBeer.getId()).isEqualTo(uuidCaptor.getValue());
    }

    @Test
    void testPatchBeer() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("beerName", "Test");

        given(beerService.patchBeerById(any(UUID.class), any(BeerDTO.class))).willReturn(Optional.of(testBeer));

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/beer/" + testBeer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        verify(beerService).patchBeerById(uuidCaptor.capture(), beerCaptor.capture());

        assertThat(testBeer.getId()).isEqualTo(uuidCaptor.getValue());
        assertThat(data.get("beerName")).isEqualTo(beerCaptor.getValue().getBeerName());
    }

    @Test
    void testGetBeerNotFound() throws Exception {
        given(beerService.getBeerById(any(UUID.class))).willThrow(NotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer/" + UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}