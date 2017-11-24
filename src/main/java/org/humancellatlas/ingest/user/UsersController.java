package org.humancellatlas.ingest.user;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/users")
public class UsersController {

    @RequestMapping(value = "/unsecured")
    @ResponseBody
    public Profile unsecured() {
        return new Profile("All good. You DO NOT need to be authenticated");
    }

    @RequestMapping(value = "/secured")
    @ResponseBody
    public Profile secured() {
        return new Profile("All good. You need to be authenticated");
    }

    public class Profile {

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        private String message;

        public Profile() {}

        public Profile(String s) {
            this.message = s;
        }

    }

}
