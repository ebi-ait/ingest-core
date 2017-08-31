package org.humancellatlas.ingest.core;

import lombok.Data;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Data
public class UpdateDate {
    private Date date;

    protected UpdateDate() {
        this.date = null;
    }

    public UpdateDate(Date date) {
        if (!isValid(date)) {
            throw new IllegalArgumentException(String.format("Update date '%s' is in the future!", date));
        }
        this.date = date;
    }

    public static boolean isValid(Date date) {
        return !date.after(new Date());
    }
}
