package jh_platform.auth.mapper;

import jh_platform.auth.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    User findByUsername(String username);

    void insertUser(User user);
}
