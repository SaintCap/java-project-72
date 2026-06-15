package hexlet.code.model;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class UrlCheck {

    private Long id;
    private Integer statusCode;
    private String title;
    private String h1;
    private String description;
    private Long urlId;
    private Timestamp createdAt;

    public String getShortTitle() {
        return truncate(title);
    }

    public String getShortH1() {
        return truncate(h1);
    }

    public String getShortDescription() {
        return truncate(description);
    }

    private String truncate(String value) {
        if (value == null) {
            return "";
        }

        return value.length() > 200
                ? value.substring(0, 200) + "..."
                : value;
    }

}
