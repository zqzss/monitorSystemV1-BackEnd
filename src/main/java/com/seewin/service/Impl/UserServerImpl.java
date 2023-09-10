package com.seewin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seewin.entity.Result;
import com.seewin.entity.User;
import com.seewin.mapper.UserMapper;
import com.seewin.service.UserService;
import com.seewin.utils.JwtUtil;
import com.seewin.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class UserServerImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    String salt = "sy";
    @Override
    public Result<HashMap<String,String>> login(User user) {

        Result<HashMap<String,String>> result = new Result();
        String password = user.getPassword();
        String encodedPassword = "";
        if(user.getUsername() == null || user.getUsername().equals("")||user.getPassword() == null || user.getPassword().equals("")){
            return new Result<>(500,null,"账号或密码不能为空！");
        }
        encodedPassword = MD5Utils.inputPassToDBPass(password,salt);
        User queryUser = userMapper.selectByUsername(user.getUsername());
        if (queryUser == null){
            return new Result<>(404,null,"账号或密码错误！");
        }
        if (queryUser.getPassword().equals(encodedPassword)){
            String token = JwtUtil.createJWT(user.getUsername());
            HashMap map = new HashMap<>();
            Integer userId = Math.toIntExact(queryUser.getId());
            map.put("token",token);
            map.put("userId",userId);
            return new Result<>(200,map,"登录成功！");
        }
        return new Result<>(500,null,"账号或密码错误！");
    }

    @Override
    public Result<HashMap<String,String>> logout(User user) {
        Result<HashMap<String,String>> result = new Result();
        HashMap map = new HashMap<>();
        map.put("token","");

        return new Result<>(200,map,"注销成功！");
    }

    @Override
    public Result getUserByQuery(HashMap<String, String> queryData) {
//        获取前端传来的数据
        String userName = queryData.get("inputUserName")!=null?queryData.get("inputUserName").toString():null;
        Integer currentPage = queryData.get("currentPage")!=null? Integer.valueOf((String) queryData.get("currentPage")) :null;
        Integer pageSize = queryData.get("pageSize")!=null? Integer.valueOf((String) queryData.get("pageSize")) :null;

        IPage<User> iPage = new Page<>(currentPage,pageSize);
        LambdaQueryWrapper<User> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(userName!=null,User::getUsername,userName);
        IPage<User> UserPage = userMapper.selectPage(iPage, lambdaQueryWrapper);

        List<User> users = UserPage.getRecords();
        Integer total = Math.toIntExact(UserPage.getTotal());

        HashMap resultData = new HashMap<>();
        resultData.put("tableData",users);
        resultData.put("total",total);
        return new Result<>(200,resultData,"查询成功！");
    }

    @Override
    public Result addUser(User user) {
        String password = user.getPassword();
        String encodedPassword = "";
        if(user.getUsername() == null || user.getUsername().equals("")||user.getPassword() == null || user.getPassword().equals("")){
            return new Result<>(500,null,"用户名或密码不能为空！");
        }
        if (user.getFullname()==null||"".equals(user.getFullname())||user.getEmail()==null||"".equals(user.getEmail())){
            return new Result<>(500,null,"全名或邮箱不能为空！");
        }
        encodedPassword = MD5Utils.inputPassToDBPass(password,salt);
        user.setPassword(encodedPassword);
        userMapper.insert(user);
        return new Result<>(200,null,"添加主机成功！");
    }

    @Override
    public Result deleteUserById(Integer id) {
        if (id!=null){
            userMapper.deleteById(id);
            return new Result<>(200,null,"删除成功！");
        }
        return new Result<>(500,null,"删除失败！");
    }

    @Override
    public Result getUserById(Integer id) {
        if (id!=null){
            User user = userMapper.selectById(id);
            return new Result<>(200,user,"查询成功！");
        }
        return new Result<>(500,null,"查询失败！");
    }

    @Override
    public Result updateUserById(User user) {
        user.setPassword(null);
        userMapper.updateById(user);
        return new Result<>(200,user,"修改成功！");
    }

    @Override
    public Result updatePwd(HashMap pwdMap) {
        String oldPassword = (String) pwdMap.get("oldPassword");
        String newPassword = (String) pwdMap.get("newPassword");
        String confirmPassword = (String) pwdMap.get("confirmPassword");
//        Integer userId = Integer.valueOf((String) pwdMap.get("userId"));
        Integer userId = (Integer) pwdMap.get("userId");
        User user = userMapper.selectById(userId);
        if(oldPassword != null && !"".equals(oldPassword)){
            String md5OldPassword = MD5Utils.inputPassToDBPass(oldPassword,salt);
            String userPassword = user.getPassword();
            if (!md5OldPassword.equals(userPassword)){
                return new Result<>(500,null,"旧密码不正确！");
            }
        }
        else {
            return new Result<>(500,null,"旧密码不能为空！");
        }
        if(newPassword != null && !"".equals(newPassword) && confirmPassword != null && !"".equals(confirmPassword)){
            if (newPassword.equals(confirmPassword)){
                String md5NewPassword = MD5Utils.inputPassToDBPass(newPassword,salt);
                user.setPassword(md5NewPassword);
                userMapper.updateById(user);
                return new Result<>(200,null,"修改密码成功！");
            }
            else {
                return new Result<>(500,null,"新密码和确认密码不一致！");
            }

        }
        else {
            return new Result<>(500,null,"新密码或确认密码不能为空！");
        }
    }
}
