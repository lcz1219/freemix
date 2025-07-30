package com.freemix.freemix.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Controller
public class BaseController {
    @Autowired
    MongoTemplate mongoTemplate;

}
