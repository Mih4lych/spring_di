package com.moh4lych.springdi.repositories;

import com.moh4lych.springdi.entities.Beer;
import com.moh4lych.springdi.model.BeerStyle;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class BeerRepositoryTest {
    @Autowired
    BeerRepository beerRepository;

    @Test
    void testSave() {
        Beer beer = beerRepository.save(Beer.builder()
                .beerName("Test")
                .beerStyle(BeerStyle.IPA)
                .upc("asd")
                .price(BigDecimal.valueOf(10, 10))
                .build());

        beerRepository.flush();
        assertThat(beer).isNotNull();
        assertThat(beer.getId()).isNotNull();
        assertThat(beer.getBeerName()).isEqualTo("Test");
    }

    @Test
    void testNameTooLongSave() {

        assertThrows(ConstraintViolationException.class, () -> {
            beerRepository.save(Beer.builder()
                    .beerName("Test 1212312133121312132121231213312131213212123121331213121321212312133121312132121231213312131213212123121331213121321212312133121312132")
                    .beerStyle(BeerStyle.IPA)
                    .upc("asd")
                    .price(BigDecimal.valueOf(10, 10))
                    .build());

            beerRepository.flush();
        });
    }
}