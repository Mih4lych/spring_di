package com.moh4lych.springdi.mappers;

import com.moh4lych.springdi.entities.Beer;
import com.moh4lych.springdi.model.BeerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface BeerMapper {
    Beer beerDtoToBeer(BeerDTO beerDTO);

    BeerDTO beerToBeerDto(Beer beer);
}
