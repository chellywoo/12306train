package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.SkToken;
import com.lxq.train.business.domain.SkTokenExample;
import com.lxq.train.business.enums.RedisKeyEnum;
import com.lxq.train.business.mapper.SkTokenMapper;
import com.lxq.train.business.mapper.customer.SkTokenCustomerMapper;
import com.lxq.train.business.req.SkTokenQueryReq;
import com.lxq.train.business.req.SkTokenSaveReq;
import com.lxq.train.business.resp.SkTokenQueryResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class SkTokenService {
    private static final Logger LOG = LoggerFactory.getLogger(SkTokenService.class);
    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
    @Resource
    private DailyTrainStationService dailyTrainStationService;
    @Resource
    private SkTokenMapper skTokenMapper;
    @Resource
    private SkTokenCustomerMapper skTokenCustomerMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    public void save(SkTokenSaveReq req){
        DateTime now = new DateTime();
        SkToken skToken = BeanUtil.copyProperties(req, SkToken.class);
        if (ObjectUtil.isNull(skToken.getId())) {
            skToken.setId(SnowUtil.getSnowFlakeNextId());
            skToken.setCreateTime(now);
            skToken.setUpdateTime(now);
            skTokenMapper.insert(skToken);
        }else{
            skToken.setUpdateTime(now);
            skTokenMapper.updateByPrimaryKey(skToken);
        }
    }

    public PageResp<SkTokenQueryResp> query(SkTokenQueryReq req){
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.setOrderByClause("id DESC");
        SkTokenExample.Criteria criteria = skTokenExample.createCriteria();

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<SkToken> skTokenList = skTokenMapper.selectByExample(skTokenExample);

        PageInfo pageInfo = new PageInfo<>(skTokenList);
        LOG.info("总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<SkTokenQueryResp> skTokenQueryResp = BeanUtil.copyToList(skTokenList, SkTokenQueryResp.class);

        PageResp<SkTokenQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(skTokenQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        skTokenMapper.deleteByPrimaryKey(id);
    }

    public void generateDaily(Date date, String trainCode){
        LOG.info("开始生成【{}】日车次【{}】令牌数据", DateUtil.formatDate(date),trainCode);

        //删除某日某车次的令牌信息
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        skTokenMapper.deleteByExample(skTokenExample);
        DateTime now = new DateTime();
        SkToken skToken = new SkToken();
        skToken.setId(SnowUtil.getSnowFlakeNextId());
        skToken.setDate(date);
        skToken.setTrainCode(trainCode);
        skToken.setCreateTime(now);
        skToken.setUpdateTime(now);

        int seatCount = dailyTrainSeatService.countByTrainCode(date, trainCode);
        LOG.info("车次【{}】座位数为：{}", trainCode, seatCount);

        long stationCount = dailyTrainStationService.countByTrainCode(date, trainCode);
        LOG.info("车次【{}】车站数为：{}", trainCode, stationCount);

        int skCount = (int) (seatCount * stationCount * 3/4);
        LOG.info("车次【{}】初始生成令牌数为：{}", trainCode, skCount);
        skToken.setCount(skCount);
        skTokenMapper.insert(skToken);
    }

    /**
     * 获取令牌
     */
    public int validSkToken(Date date, String trainCode, Long memberId) {
        LOG.info("用户【{}】获取日期【{}】车次【{}】的令牌开始", memberId, DateUtil.formatDate(date), trainCode);

        // 用户先获取令牌锁，key就是令牌，表示谁能做什么的凭证
        // 拿到锁进行操作之后不进行释放，保证5s内进行的操作不能再次被处理
        String key = RedisKeyEnum.SK_TOKEN + "-" +DateUtil.formatDate(date) + "-" + trainCode + memberId;
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(key, key, 5, TimeUnit.SECONDS);
        if(Boolean.TRUE.equals(setIfAbsent)){
            LOG.info("恭喜抢到令牌锁了，lockKey:{}", key);
        }else{
            LOG.info("很遗憾没抢到令牌锁，lockKey:{}", key);
            return -1;
        }
        String skTokenCountKey = RedisKeyEnum.SK_TOKEN_COUNT + "-" + DateUtil.formatDate(date) + "-" + trainCode;
        Object skTokenCount = redisTemplate.opsForValue().get(skTokenCountKey);
        if (skTokenCount != null) {
            LOG.info("缓存中有该车次令牌大闸的key：{}", skTokenCountKey);
            Long count = redisTemplate.opsForValue().decrement(skTokenCountKey, 1);

            if (count < 0L) {
                LOG.error("获取令牌失败：{}", skTokenCountKey);
                return 0;
            } else {
                LOG.info("获取令牌后，令牌余数：{}", count);
                // 缓存不断刷新过期时间60s，防止key失效，因为令牌一直存在
                redisTemplate.expire(skTokenCountKey, 60, TimeUnit.SECONDS);
                // 每获取5个令牌更新一次数据库
                if (count % 5 == 0) {
                    skTokenCustomerMapper.decrease(date, trainCode, 5);
                }
                return 1;
            }
        } else {
            LOG.info("缓存中没有该车次令牌大闸的key：{}", skTokenCountKey);
            // 检查是否还有令牌
            SkTokenExample skTokenExample = new SkTokenExample();
            skTokenExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
            List<SkToken> tokenCountList = skTokenMapper.selectByExample(skTokenExample);
            if (CollUtil.isEmpty(tokenCountList)) {
                LOG.info("找不到日期【{}】车次【{}】的令牌记录", DateUtil.formatDate(date), trainCode);
                return 0;
            }

//            SkToken skToken = tokenCountList.get(0);
            int count = tokenCountList.get(0).getCount();
//            if (skToken.getCount() <= 0) {
            if (count <= 0) {
                LOG.info("日期【{}】车次【{}】的令牌余量为0", DateUtil.formatDate(date), trainCode);
                return 0;
            }

            // 令牌还有余量
            // 令牌余数-1
//            Integer count = skToken.getCount() - 1;
//            skToken.setCount(count);
            LOG.info("将该车次令牌大闸放入缓存中，key: {}， count: {}", skTokenCountKey, count);
            // 不需要更新数据库，只要放缓存即可
            redisTemplate.opsForValue().set(skTokenCountKey, String.valueOf(count), 60, TimeUnit.SECONDS);
            //skTokenMapper.updateByPrimaryKey(skToken);
            return 1;
        }

//        // 令牌约等于库存，令牌没有了，就不再卖票，不需要再进入购票主流程去判断库存，判断令牌肯定比判断库存效率高
//        int updateCount = skTokenCustomerMapper.decrease(date, trainCode, 1);
//        if (updateCount > 0) {
//            return 1;
//        } else {
//            return 0;
//        }
    }
}
