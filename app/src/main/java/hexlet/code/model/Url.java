package hexlet.code.model;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Url {

    private Long id;
    private String name;
    private Timestamp createdAt;

}
