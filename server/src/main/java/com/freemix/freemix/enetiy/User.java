package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class User {
    String id;
    String username;
    String email;
    String password;
    String saveQuestionOne;
    String saveAnOne;
    String saveQuestionTwice;
    String saveAnTwice;
    String saveQuestionThree;
    String saveAnThree;
    String token;

}
