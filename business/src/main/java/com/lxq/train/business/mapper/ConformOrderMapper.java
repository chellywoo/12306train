package com.lxq.train.business.mapper;

import com.lxq.train.business.domain.ConformOrder;
import com.lxq.train.business.domain.ConformOrderExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ConformOrderMapper {
    long countByExample(ConformOrderExample example);

    int deleteByExample(ConformOrderExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ConformOrder record);

    int insertSelective(ConformOrder record);

    List<ConformOrder> selectByExampleWithBLOBs(ConformOrderExample example);

    List<ConformOrder> selectByExample(ConformOrderExample example);

    ConformOrder selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ConformOrder record, @Param("example") ConformOrderExample example);

    int updateByExampleWithBLOBs(@Param("record") ConformOrder record, @Param("example") ConformOrderExample example);

    int updateByExample(@Param("record") ConformOrder record, @Param("example") ConformOrderExample example);

    int updateByPrimaryKeySelective(ConformOrder record);

    int updateByPrimaryKeyWithBLOBs(ConformOrder record);

    int updateByPrimaryKey(ConformOrder record);
}