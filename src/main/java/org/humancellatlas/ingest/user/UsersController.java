package org.humancellatlas.ingest.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/users")
public class UsersController {

    @Autowired
    SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @RequestMapping(value = "/unsecured")
    @ResponseBody
    public Profile unsecured() {
        return new Profile("All good. You DO NOT need to be authenticated");
    }

    @RequestMapping(value = "/secured")
    @ResponseBody
    public Profile secured() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new Profile("All good. You were authenticated as: " + principal.toString());
    }

    @RequestMapping(value = "/summary")
    @ResponseBody
    public Summary summary() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        int draftSubmissions = submissionEnvelopeRepository.findBySubmissionState(SubmissionState.DRAFT).size();
        int completedSubmissions = submissionEnvelopeRepository.findBySubmissionState(SubmissionState.COMPLETE).size();
        return new Summary(principal.toString(),  draftSubmissions, completedSubmissions, "420 GB");
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public class Summary {

        private String principal = "";
        private Integer draftSubmissions = 0;
        private Integer completedSubmissions = 0;
        private String storageSize = "0 MB";

        public Summary(String principal, int draftSubmissions, int completedSubmissions, String storageSize) {
            this.principal = principal;
            this.draftSubmissions = draftSubmissions;
            this.completedSubmissions = completedSubmissions;
            this.storageSize = storageSize;
        }
    }

    public class Profile {

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        private String message;

        public Profile() {
        }

        public Profile(String s) {
            this.message = s;
        }

    }

}
