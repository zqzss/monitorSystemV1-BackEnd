package com.seewin.service;

import com.seewin.entity.Result;
import com.seewin.entity.User;

import java.util.HashMap;

public interface UserService {
    Result<HashMap<String,String>> login(User user);

    Result logout(User user);
    Result getUserByQuery(HashMap<String,String> queryData);
    Result addUser(User user);
    Result deleteUserById(Integer id);
    Result getUserById(Integer id);
    Result updateUserById(User user);
    Result updatePwd(HashMap pwdMap);
}
