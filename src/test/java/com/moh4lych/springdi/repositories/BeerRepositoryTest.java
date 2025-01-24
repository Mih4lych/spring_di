package com.moh4lych.springdi.repositories;

import com.moh4lych.springdi.bootstrap.BootstrapData;
import com.moh4lych.springdi.entities.Beer;
import com.moh4lych.springdi.model.BeerStyle;
import com.moh4lych.springdi.services.BeerCSVServiceImpl;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import({BootstrapData.class, BeerCSVServiceImpl.class})
class BeerRepositoryTest {
    @Autowired
    BeerRepository beerRepository;

    @Test
    void testFindAllByNameAndStyle() {
        Page<Beer> beers = beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%IPA%", BeerStyle.IPA, null);

        assertThat(beers.getContent().size()).isEqualTo(310);
    }


    @Test
    void testFindAllByName() {
        Page<Beer> beers = beerRepository.findAllByBeerNameIsLikeIgnoreCase("%IPA%", null);

        assertThat(beers.getContent().size()).isEqualTo(336);
    }

    @Test
    void testFindAllByStyle() {
        Page<Beer> beers = beerRepository.findAllByBeerStyle(BeerStyle.IPA, null);

        assertThat(beers.getContent().size()).isEqualTo(548);
    }

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