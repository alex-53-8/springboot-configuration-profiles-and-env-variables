package io.alex538;

import io.alex538.cfg.GlobalConfiguration;
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
