package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "tag")
public class Tag {
    @Id
    private String id;
    private String name;
}
