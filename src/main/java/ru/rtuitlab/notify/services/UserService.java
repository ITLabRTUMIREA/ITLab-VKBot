package ru.rtuitlab.notify.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.rtuitlab.notify.models.User;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final RestTemplate restTemplate;

    @Value("${secrets.token}")
    private String token;
    @Value("${secrets.url}")
    private String url;
    @Value("${secrets.query}")
    private String query;

    public UserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<User> getUsers() {
        HttpEntity<String> request = getHeaders();
        ResponseEntity<User[]> response = restTemplate.exchange(url + query, HttpMethod.GET, request, User[].class);
        if (response.getBody() == null) {
            log.error("Can't update users info");
            return null;
        }
        List<User> users = Arrays.asList(response.getBody());
        log.info("Users information has been updated");
        return users;
    }

    public User getUser(String userId) {
        User res = null;
        for (User user : getUsers()) {
            if (user.getId().equals(userId)) {
                res = user;
                break;
            }
        }
        return res;
//        HttpEntity<String> request = getHeaders();
//        ResponseEntity<User> response = restTemplate.exchange(
//                url + '/' + userId,
//                HttpMethod.GET,
//                request,
//                User.class
//        );
//        if (response.getBody() == null) {
//            log.error("Can't get user " + userId + " info");
//            return null;
//        }
//        User user = response.getBody();
//        log.info("User information has been updated");
//        return user;
    }

    private HttpEntity<String> getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Key", token);
        return new HttpEntity<>(headers);
    }
}
