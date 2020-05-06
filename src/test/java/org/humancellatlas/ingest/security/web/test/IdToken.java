package org.humancellatlas.ingest.security.web.test;

public class IdToken {

    private final String subject;
    private final String name;

    public IdToken(String subject, String name) {
        this.subject = subject;
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public String getName() {
        return name;
    }

    public String toJwt() {
        return new StringBuilder("{")
                .append("sub: ").append(subject)
                .append("name: ").append(name)
                .append("}")
                .toString();
    }

}
