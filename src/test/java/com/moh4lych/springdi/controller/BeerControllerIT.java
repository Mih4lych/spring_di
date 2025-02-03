package com.moh4lych.springdi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moh4lych.springdi.entities.Beer;
import com.moh4lych.springdi.events.BeerCreatedEvent;
import com.moh4lych.springdi.model.BeerDTO;
import com.moh4lych.springdi.model.BeerStyle;
import com.moh4lych.springdi.repositories.BeerRepository;
import jakarta.transaction.Transactional;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RecordApplicationEvents
@SpringBootTest
class BeerControllerIT {
    @Autowired
    BeerController beerController;
    @Autowired
    BeerRepository beerRepository;
    @Autowired
    ApplicationEvents applicationEvents;

    @Autowired
    WebApplicationContext wac;

    @Autowired
    ObjectMapper objectMapper;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void testListBeersPaging() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer/")
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "50")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content.length()", is(50)));
    }

    @Test
    void testListBeersByNameAndStyle() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer/")
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content.length()", is(25)));
    }

    @Test
    void testListBeersByName() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer/")
                        .queryParam("beerName", "IPA"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content.length()", is(25)));
    }

    @Test
    void testListBeersByStyle() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/beer/")
                        .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content.length()", is(25)));
    }

    @Test
    void testListBeers() {
        Page<BeerDTO> list = beerController.listBeers(null, null, 1, 25);

        Assertions.assertEquals(25, list.getContent().size());
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyListBeers() {
        beerRepository.deleteAll();
        Page<BeerDTO> list = beerController.listBeers(null, null, 1, 25);

        assertThat(list.getContent().size()).isEqualTo(0);
    }

    @Test
    void testGetBeerById() {
        Beer beer = beerRepository.findAll().getFirst();
        BeerDTO beerDTO = beerController.getBeerById(beer.getId());

        assertThat(beerDTO).isNotNull();
    }

    @Test
    void testGetBeerByIdWithError() {
        assertThrows(NotFoundException.class, () -> {
            beerController.getBeerById(UUID.randomUUID());
        });
    }

    @Rollback
    @Transactional
    @Test
    void testSaveNewBeer() {
        BeerDTO beerDTO = BeerDTO.builder().beerName("Test").upc("112233").build();

        ResponseEntity response = beerController.handlePost(beerDTO);
        URI location = response.getHeaders().getLocation();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(location).isNotNull();

        UUID beerId = UUID.fromString(location.getPath().split("/")[4]);
        Beer beer = beerRepository.findById(beerId).get();

        assertThat(beer).isNotNull();
        val count = applicationEvents.stream(BeerCreatedEvent.class).count();

        Assertions.assertEquals(1, count);
    }

    @Rollback
    @Transactional
    @Test
    void testUpdateBeer() {
        BeerDTO beerDTO = BeerDTO.builder().beerName("Test").upc("112233").build();
        BeerDTO beerDTOToChange = beerController.listBeers(null, null, 1, 25).getContent().getFirst();

        ResponseEntity response = beerController.updateById(beerDTOToChange.getId(), beerDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Beer beer = beerRepository.getReferenceById(beerDTOToChange.getId());

        assertThat(beer.getBeerName()).isEqualTo("Test");
        assertThat(beer.getUpc()).isEqualTo("112233");
    }

    @Test
    void testUpdateBeerNotFound() {
        assertThrows(NotFoundException.class, () -> {
            beerController.updateById(UUID.randomUUID(), BeerDTO.builder().build());
        });
    }

    @Rollback
    @Transactional
    @Test
    void testDeleteBeer() {
        BeerDTO beerDTOToChange = beerController.listBeers(null, null, 1, 25).getContent().getFirst();

        ResponseEntity response = beerController.deleteById(beerDTOToChange.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(beerRepository.findById(beerDTOToChange.getId())).isEqualTo(Optional.empty());
    }

    @Test
    void testDeleteBeerNotFound() {
        assertThrows(NotFoundException.class, () -> {
            beerController.deleteById(UUID.randomUUID());
        });
    }

    @Rollback
    @Transactional
    @Test
    void testPatchBeer() {
        BeerDTO beerDTOToChange = beerController.listBeers(null, null, 1, 25).getContent().getFirst();

        ResponseEntity response = beerController.patchById(beerDTOToChange.getId(), BeerDTO.builder().beerName("Test").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Optional<Beer> beerChanged = beerRepository.findById(beerDTOToChange.getId());

        assertTrue(beerChanged.isPresent());
        assertEquals("Beer name", "Test", beerChanged.get().getBeerName());
    }

    @Test
    void testPatchBeerNotFound() {
        assertThrows(NotFoundException.class, () -> {
            beerController.patchById(UUID.randomUUID(), BeerDTO.builder().build());
        });
    }

    @Test
    void testPatchBeerLongName() throws Exception {
        Beer beer = beerRepository.findAll().getFirst();
        Map<String, Object> data = new HashMap<>();
        data.put("beerName", "Test 123123333333333132123123123333333333132123123123333333333132123123123333333333132123123123333333333132123123123333333333132123");

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/beer/" + beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}