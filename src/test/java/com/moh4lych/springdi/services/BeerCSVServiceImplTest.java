package com.moh4lych.springdi.services;

import com.moh4lych.springdi.model.BeerCSVRecord;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BeerCSVServiceImplTest {
    BeerCSVServiceImpl beerCSVService = new BeerCSVServiceImpl();

    @Test
    void checkConversion() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:csvdata/beer.csv");
        List<BeerCSVRecord> list = beerCSVService.convertCSV(file);

        assertFalse(list.isEmpty());
    }

}