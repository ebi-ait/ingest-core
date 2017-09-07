package org.humancellatlas.ingest.file.web;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by rolando on 07/09/2017.
 */
@Getter
@AllArgsConstructor
public class FileMessage {
    private final String cloudUrl;
    private final String fileName;
    private final UUID envelopeUuid;
}