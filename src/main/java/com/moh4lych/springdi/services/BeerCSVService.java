package com.moh4lych.springdi.services;

import com.moh4lych.springdi.model.BeerCSVRecord;

import java.io.File;
import java.util.List;

public interface BeerCSVService {
    public List<BeerCSVRecord> convertCSV(File csv);
}
