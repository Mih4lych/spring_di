package com.moh4lych.springdi.services;

import com.moh4lych.springdi.mappers.BeerMapper;
import com.moh4lych.springdi.model.BeerDTO;
import com.moh4lych.springdi.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {
    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    @Override
    public List<BeerDTO> listBeers() {
        return beerRepository
                .findAll()
                .stream()
                .map(beerMapper::beerToBeerDto)
                .toList();
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        return beerRepository.findById(id).map(beerMapper::beerToBeerDto);
    }

    @Override
    public BeerDTO saveNewBeer(BeerDTO beerDTO) {
        return beerMapper.beerToBeerDto(beerRepository.save(beerMapper.beerDtoToBeer(beerDTO)));
    }

    @Override
    public Optional<BeerDTO> updateBeerById(UUID beerId, BeerDTO beerDTO) {
        return beerRepository.findById(beerId).map(foundBeer -> {
            foundBeer.setBeerName(beerDTO.getBeerName());
            foundBeer.setBeerStyle(beerDTO.getBeerStyle());
            foundBeer.setUpc(beerDTO.getUpc());
            foundBeer.setPrice(beerDTO.getPrice());
            return beerMapper.beerToBeerDto(beerRepository.save(foundBeer));
        });
    }

    @Override
    public Optional<BeerDTO> deleteBeerById(UUID beerId) {
        return beerRepository.findById(beerId).map(foundBeer -> {
            beerRepository.deleteById(beerId);
            return beerMapper.beerToBeerDto(foundBeer);
        });
    }

    @Override
    public Optional<BeerDTO> patchBeerById(UUID beerId, BeerDTO beerDTO) {
        return beerRepository.findById(beerId).map(existing -> {
            if (StringUtils.hasText(beerDTO.getBeerName())) {
                existing.setBeerName(beerDTO.getBeerName());
            }

            if (beerDTO.getBeerStyle() != null) {
                existing.setBeerStyle(beerDTO.getBeerStyle());
            }

            if (beerDTO.getPrice() != null) {
                existing.setPrice(beerDTO.getPrice());
            }

            if (beerDTO.getQuantityOnHand() != null) {
                existing.setQuantityOnHand(beerDTO.getQuantityOnHand());
            }

            if (StringUtils.hasText(beerDTO.getUpc())) {
                existing.setUpc(beerDTO.getUpc());
            }
            beerRepository.save(existing);

            return beerMapper.beerToBeerDto(existing);
        });
    }
}
