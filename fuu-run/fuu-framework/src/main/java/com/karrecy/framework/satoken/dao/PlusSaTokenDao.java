package com.karrecy.framework.satoken.dao;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.util.SaFoxUtil;
import com.karrecy.common.utils.redis.RedisUtils;
import com.karrecy.common.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Sa-Token持久层接口
 */
@Slf4j
public class PlusSaTokenDao implements SaTokenDao {

    /**
     * 获取Value，如无返空
     */
    @Override
    public String get(String key) {
        return RedisUtils.getCacheObject(key);
    }

    /**
     * 写入Value，并设定存活时间 (单位: 秒)
     */
    @Override
    public void set(String key, String value, long timeout) {
        if (timeout == 0 || timeout <= SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            RedisUtils.setCacheObject(key, value);
        } else {
            RedisUtils.setCacheObject(key, value, Duration.ofSeconds(timeout));
        }
    }

    /**
     * 修修改指定key-value键值对 (过期时间不变)
     */
    @Override
    public void update(String key, String value) {
        long expire = getTimeout(key);
        // -2 = 无此键
        if (expire == SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        this.set(key, value, expire);
    }

    /**
     * 删除Value
     */
    @Override
    public void delete(String key) {
        RedisUtils.deleteObject(key);
    }

    /**
     * 获取Value的剩余存活时间 (单位: 秒)
     */
    @Override
    public long getTimeout(String key) {
        long timeout = RedisUtils.getTimeToLive(key);
        return timeout < 0 ? timeout : timeout / 1000;
    }

    /**
     * 修改Value的剩余存活时间 (单位: 秒)
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        // 判断是否想要设置为永久
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            long expire = getTimeout(key);
            if (expire == SaTokenDao.NEVER_EXPIRE) {
                // 如果其已经被设置为永久，则不作任何处理
            } else {
                // 如果尚未被设置为永久，那么再次set一次
                this.set(key, this.get(key), timeout);
            }
            return;
        }
        RedisUtils.expire(key, Duration.ofSeconds(timeout));
    }


    /**
     * 获取Object，如无返空
     */
    @Override
    public Object getObject(String key) {
        return RedisUtils.getCacheObject(key);
    }

    /**
     * 获取指定类型Object（Sa-Token 1.4x 新增；语义参考官方 SaTokenDaoBySessionFollowObject）
     * <p>Redis 中以纯 JSON 存储（不含类型信息），反序列化后为 Map，这里按目标类型做还原。</p>
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(String key, Class<T> classType) {
        Object obj = RedisUtils.getCacheObject(key);
        if (obj == null) {
            return null;
        }
        if (classType == null || classType.isInstance(obj)) {
            return (T) obj;
        }
        try {
            return SpringUtils.getBean(ObjectMapper.class).convertValue(obj, classType);
        } catch (Exception e) {
            log.warn("Sa-Token 缓存对象类型还原失败：key={}, target={}, actual={}", key, classType.getName(), obj.getClass().getName());
            return (T) obj;
        }
    }

    /**
     * 写入Object，并设定存活时间 (单位: 秒)
     */
    @Override
    public void setObject(String key, Object object, long timeout) {
        if (timeout == 0 || timeout <= SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            RedisUtils.setCacheObject(key, object);
        } else {
            RedisUtils.setCacheObject(key, object, Duration.ofSeconds(timeout));
        }
    }

    /**
     * 更新Object (过期时间不变)
     */
    @Override
    public void updateObject(String key, Object object) {
        long expire = getObjectTimeout(key);
        // -2 = 无此键
        if (expire == SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        this.setObject(key, object, expire);
    }

    /**
     * 删除Object
     */
    @Override
    public void deleteObject(String key) {
        RedisUtils.deleteObject(key);
    }

    /**
     * 获取Object的剩余存活时间 (单位: 秒)
     */
    @Override
    public long getObjectTimeout(String key) {
        long timeout = RedisUtils.getTimeToLive(key);
        return timeout < 0 ? timeout : timeout / 1000;
    }

    /**
     * 修改Object的剩余存活时间 (单位: 秒)
     */
    @Override
    public void updateObjectTimeout(String key, long timeout) {
        // 判断是否想要设置为永久
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            long expire = getObjectTimeout(key);
            if (expire == SaTokenDao.NEVER_EXPIRE) {
                // 如果其已经被设置为永久，则不作任何处理
            } else {
                // 如果尚未被设置为永久，那么再次set一次
                this.setObject(key, this.getObject(key), timeout);
            }
            return;
        }
        RedisUtils.expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 获取 SaSession，如无返空（Sa-Token 1.41+ 新增接口，复用 Object 读写）
     */
    @Override
    public SaSession getSession(String sessionId) {
        return getObject(sessionId, SaSession.class);
    }

    /**
     * 写入 SaSession，并设定存活时间（单位: 秒）
     */
    @Override
    public void setSession(SaSession session, long timeout) {
        setObject(session.getId(), session, timeout);
    }

    /**
     * 更新 SaSession
     */
    @Override
    public void updateSession(SaSession session) {
        updateObject(session.getId(), session);
    }

    /**
     * 删除 SaSession
     */
    @Override
    public void deleteSession(String sessionId) {
        deleteObject(sessionId);
    }

    /**
     * 获取 SaSession 剩余存活时间（单位: 秒）
     */
    @Override
    public long getSessionTimeout(String sessionId) {
        return getObjectTimeout(sessionId);
    }

    /**
     * 修改 SaSession 剩余存活时间（单位: 秒）
     */
    @Override
    public void updateSessionTimeout(String sessionId, long timeout) {
        updateObjectTimeout(sessionId, timeout);
    }


    /**
     * 搜索数据
     */
    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        Collection<String> keys = RedisUtils.keys(prefix + "*" + keyword + "*");
        List<String> list = new ArrayList<>(keys);
        return SaFoxUtil.searchList(list, start, size, sortType);
    }
}
