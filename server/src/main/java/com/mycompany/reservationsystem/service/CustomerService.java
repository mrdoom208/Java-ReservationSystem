package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.model.Customer;
import com.mycompany.reservationsystem.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> searchCustomers(String query) {
        return customerRepository.findByNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(query, query);
    }
}
