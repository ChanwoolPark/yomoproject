package com.project.yomozomo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.project.yomozomo.domain.WalletLog;
import java.util.List;

@Mapper
public interface WalletLogMapper {
    void insertLog(WalletLog log);
    List<WalletLog> findLogsByUserWalletId(@Param("userWalletId") Long userWalletId);


}


