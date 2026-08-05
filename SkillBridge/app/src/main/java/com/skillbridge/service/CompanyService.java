package com.skillbridge.service;

import com.skillbridge.entity.Company;
import com.skillbridge.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    public Company findById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + id));
    }

    public Company findOrCreateByName(String name) {
        return companyRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Company c = new Company();
                    c.setName(name);
                    return companyRepository.save(c);
                });
    }

    public Company save(Company company) {
        return companyRepository.save(company);
    }

    public void setVerified(Long id, boolean verified) {
        Company c = findById(id);
        c.setVerified(verified);
        companyRepository.save(c);
    }
}
