package org.humancellatlas.ingest.security.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/auth")
public class AuthenticationController {

    @PostMapping("/registration")
    public void register() {

    }

}
