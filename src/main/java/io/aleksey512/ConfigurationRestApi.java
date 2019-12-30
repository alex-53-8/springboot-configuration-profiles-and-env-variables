package io.aleksey512;

import io.aleksey512.cfg.GlobalConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigurationRestApi {

    @Autowired
    GlobalConfiguration globalConfiguration;

    @GetMapping("/configuration")
    public ResponseEntity configuration() {
        return ResponseEntity.ok(globalConfiguration);
    }

}
