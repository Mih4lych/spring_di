package com.moh4lych.springdi.services;

import com.moh4lych.springdi.model.BeerCSVRecord;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

@Service
@NoArgsConstructor
public class BeerCSVServiceImpl implements BeerCSVService {
    @Override
    public List<BeerCSVRecord> convertCSV(File csv) {
        try {
            return new CsvToBeanBuilder<BeerCSVRecord>(new FileReader(csv))
                    .withType(BeerCSVRecord.class)
                    .build()
                    .parse();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
