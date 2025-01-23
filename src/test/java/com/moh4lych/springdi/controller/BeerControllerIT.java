package com.moh4lych.springdi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moh4lych.springdi.entities.Beer;
import com.moh4lych.springdi.model.BeerDTO;
import com.moh4lych.springdi.repositories.BeerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest
class BeerControllerIT {
    @Autowired
    BeerController beerController;
    @Autowired
    BeerRepository beerRepository;

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
    void testListBeers() {
        List<BeerDTO> list = beerController.listBeers();

        assertTrue(list.size() > 10);
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyListBeers() {
        beerRepository.deleteAll();
        List<BeerDTO> list = beerController.listBeers();

        assertThat(list.size()).isEqualTo(0);
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
    }

    @Rollback
    @Transactional
    @Test
    void testUpdateBeer() {
        BeerDTO beerDTO = BeerDTO.builder().beerName("Test").upc("112233").build();
        BeerDTO beerDTOToChange = beerController.listBeers().getFirst();

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
        BeerDTO beerDTOToChange = beerController.listBeers().getFirst();

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
        BeerDTO beerDTOToChange = beerController.listBeers().getFirst();

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