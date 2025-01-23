package com.moh4lych.springdi.bootstrap;

import com.moh4lych.springdi.repositories.BeerRepository;
import com.moh4lych.springdi.services.BeerCSVService;
import com.moh4lych.springdi.services.BeerCSVServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(BeerCSVServiceImpl.class)
class BootstrapDataTest {

    @Autowired
    BeerRepository beerRepository;
    @Autowired
    BeerCSVService beerCSVService;

    BootstrapData bootstrapData;

    @BeforeEach
    void setUp() {
        bootstrapData = new BootstrapData(beerRepository, beerCSVService);
    }

    @Test
    void testData() throws Exception {
        bootstrapData.run(null);

        assertTrue(beerRepository.count() > 10);
    }
}