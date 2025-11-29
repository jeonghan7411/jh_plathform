package jh_platform.auth.mapper;

import jh_platform.auth.model.RefreshToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper {

    RefreshToken findByUsername(String username);

    void upsertRefreshToken(RefreshToken refreshToken);

    void deleteByUsername(String username);
}
