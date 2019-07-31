package com.atuinfo.user;

import com.atuinfo.common.model.User;
import com.jfinal.plugin.activerecord.Page;

import java.util.List;

public class UserService {

    private User userDao = new User().dao();

    public Page<User> paginate(int pageNumber, int pageSize) {
        return userDao.paginate(pageNumber, pageSize, "select *", "from user");
    }

    public List<User> getAll(){
        return userDao.find("select * from user");
    }

    public User findById(int id) {
        return userDao.findById(id);
    }

    public void deleteById(int id) {
        userDao.deleteById(id);
    }
}
