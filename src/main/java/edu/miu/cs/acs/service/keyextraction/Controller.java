package edu.miu.cs.acs.service.keyextraction;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class Controller {

    @Autowired
    private KeyExtraction keyExtraction;

    @GetMapping("/test")
    public Set<String> test() {
        return keyExtraction.getKeys("https://api.orb-intelligence.com/docs/", 2);
    }
}
