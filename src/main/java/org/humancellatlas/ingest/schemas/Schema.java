package org.humancellatlas.ingest.schemas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.humancellatlas.ingest.core.AbstractEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Getter
public class Schema extends AbstractEntity implements Comparable<Schema> {

    private final String highLevelEntity;
    private final String schemaVersion;
    private final String domainEntity;
    private final String subDomainEntity;
    private final String concreteEntity;

    @JsonIgnore
    private final String schemaUri;

    @Override
    public int compareTo(Schema other) {
        SemanticVersion otherSchemaVersion = SemanticVersion.parse(other.schemaVersion);
        return SemanticVersion.parse(schemaVersion).compareTo(otherSchemaVersion);
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
