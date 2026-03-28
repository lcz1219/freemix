package com.freemix.freemix.service;

import com.freemix.freemix.enetiy.User;

public interface UserService {
    User findByGithubId(String githubId);
    User findByQqOpenId(String qqOpenId);
    User findByToken(String token);
    User save(User user);
}