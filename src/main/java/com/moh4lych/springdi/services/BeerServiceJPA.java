package com.moh4lych.springdi.services;

import com.moh4lych.springdi.entities.Beer;
import com.moh4lych.springdi.events.BeerCreatedEvent;
import com.moh4lych.springdi.mappers.BeerMapper;
import com.moh4lych.springdi.model.BeerDTO;
import com.moh4lych.springdi.model.BeerStyle;
import com.moh4lych.springdi.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {
    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 25;

    @Override
    public Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Integer pageNumber, Integer pageSize) {
        Page<Beer> beers;
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        if (StringUtils.hasText(beerName) && Objects.isNull(beerStyle)) {
            beers = listBeersByName(beerName, pageRequest);
        } else if (!StringUtils.hasText(beerName) && Objects.nonNull(beerStyle)) {
            beers = listBeersByStyle(beerStyle, pageRequest);
        } else if (StringUtils.hasText(beerName) && beerStyle != null) {
            beers = listBeersByNameAndStyle(beerName, beerStyle, pageRequest);
        } else {
            beers = beerRepository.findAll(pageRequest);
        }

        return beers.map(beerMapper::beerToBeerDto);
    }

    private Page<Beer> listBeersByNameAndStyle(String beerName, BeerStyle beerStyle, PageRequest pageRequest) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%" + beerName + "%", beerStyle, pageRequest);
    }

    private Page<Beer> listBeersByName(String beerName, PageRequest pageRequest) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%", pageRequest);
    }

    private Page<Beer> listBeersByStyle(BeerStyle beerStyle, PageRequest pageRequest) {
        return beerRepository.findAllByBeerStyle(beerStyle, pageRequest);
    }

    private PageRequest buildPageRequest(Integer pageNumber, Integer pageSize) {
        int queryPageNumber = DEFAULT_PAGE_NUMBER;
        int queryPageSize = DEFAULT_PAGE_SIZE;

        if (Objects.nonNull(pageNumber) && pageNumber > 0) {
            queryPageNumber = pageNumber - 1;
        }
        if (Objects.nonNull(pageSize)) {
            queryPageSize = pageSize;
        }

        return PageRequest.of(queryPageNumber, queryPageSize);
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        return beerRepository.findById(id).map(beerMapper::beerToBeerDto);
    }

    @Override
    public BeerDTO saveNewBeer(BeerDTO beerDTO) {
        val newBeer = beerRepository.save(beerMapper.beerDtoToBeer(beerDTO));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        applicationEventPublisher.publishEvent(new BeerCreatedEvent(newBeer, authentication));

        return beerMapper.beerToBeerDto(newBeer);
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
