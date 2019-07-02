package org.humancellatlas.ingest.schemas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.humancellatlas.ingest.core.AbstractEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Getter
public class Schema extends AbstractEntity implements Comparable<Schema> {

    private String highLevelEntity;
    private String schemaVersion;
    private String domainEntity;
    private String subDomainEntity;
    private String concreteEntity;

    private String compoundKeys;

    @JsonIgnore
    private String schemaUri;

    public Schema(String highLevelEntity, String schemaVersion, String domainEntity,
            String subDomainEntity, String concreteEntity, String schemaUri) {
        this.highLevelEntity = highLevelEntity;
        this.schemaVersion = schemaVersion;
        this.domainEntity = domainEntity;
        this.subDomainEntity = subDomainEntity;
        this.concreteEntity = concreteEntity;
        this.schemaUri = schemaUri;
        this.compoundKeys = concatenateKeys();
    }

    private String concatenateKeys() {
        return format("%s/%s/%s/%s", highLevelEntity, domainEntity, subDomainEntity,
                concreteEntity);
    }

    @Override
    public int compareTo(Schema other) {
        int difference = compoundKeys.compareTo(other.compoundKeys);
        if (difference == 0) {
            SemanticVersion otherSchemaVersion = SemanticVersion.parse(other.schemaVersion);
            difference = SemanticVersion.parse(schemaVersion).compareTo(otherSchemaVersion);
        }
        return difference;
    }

    private static class SemanticVersion implements Comparable<SemanticVersion> {

        private static final Pattern VERSION_PATTERN = Pattern.compile("(?<major>\\p{Digit}+)" +
                "(.(?<minor>\\p{Digit}+))??(.(?<patch>\\p{Digit}+))??");

        final int major;
        final int minor;
        int patch;

        SemanticVersion(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        static SemanticVersion parse(String version) {
            Matcher match = VERSION_PATTERN.matcher(version);
            if (match.matches()) {
                int major = parseVersionSegment(match, "major");
                int minor = parseVersionSegment(match, "minor");
                int patch = parseVersionSegment(match, "patch");
                return new SemanticVersion(major, minor, patch);
            } else {
                throw new RuntimeException("Invalid version format.");
            }
        }

        private static int parseVersionSegment(Matcher match, String versionSegment) {
            String segmentValue = match.group(versionSegment);
            int value = 0;
            if (segmentValue != null) {
                value = Integer.parseInt(segmentValue);
            }
            return value;
        }

        @Override
        public int compareTo(SemanticVersion other) {
            int difference = major - other.major;
            if (difference == 0) {
                difference = minor - other.minor;
                if (difference == 0) {
                    difference = patch - other.patch;
                }
            }
            return difference;
        }

    }

}
