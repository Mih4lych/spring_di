package com.moh4lych.springdi.mappers;

import com.moh4lych.springdi.entities.Customer;
import com.moh4lych.springdi.model.CustomerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {

    Customer customerDtoToCustomer(CustomerDTO dto);

    CustomerDTO customerToCustomerDto(Customer customer);

}
