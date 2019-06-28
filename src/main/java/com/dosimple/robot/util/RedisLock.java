package com.dosimple.robot.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Redis 锁
 **/
@Slf4j
public class RedisLock {

    private RedisTemplate redisTemplate = null;

    private static final long LOCK_TIMEOUT = 1000 * 5;

    private static final RedisLock lock = new RedisLock();

    private RedisLock() {
        redisTemplate = SpringContextHolder.getBean("redisTemplate");
    }

    public static RedisLock getInstance() {
        return lock;
    }

    /**
     * 加锁
     * 取到锁加锁，取不到锁一直等待知道获得锁
     *
     * @param lockKey
     * @param threadName
     * @return
     */
    public synchronized long lock(String lockKey, String threadName) {
        while (true) {
            //锁时间
            Long lockTimeout = currtTimeForRedis() + LOCK_TIMEOUT + 1;
            if (redisTemplate.opsForValue().setIfAbsent(lockKey, lockTimeout)) {
                //设置超时时间，释放内存
                redisTemplate.expire(lockKey, LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
                return lockTimeout;
            } else {
                //获取redis里面的时间
                Object result = redisTemplate.opsForValue().get(lockKey);
                Long currtLockTimeoutStr = result == null ? null : Long.valueOf(String.valueOf(result));
                //锁已经失效
                if (currtLockTimeoutStr != null && currtLockTimeoutStr < currtTimeForRedis()) {
                    //判断是否为空，不为空时，说明已经失效，如果被其他线程设置了值，则第二个条件判断无法执行
                    //获取上一个锁到期时间，并设置现在的锁到期时间
                    Object oldTimeout = redisTemplate.opsForValue().getAndSet(lockKey, lockTimeout);
                    Long old_lock_timeout_Str = oldTimeout == null ? null : Long.valueOf(String.valueOf(oldTimeout));
                    if (old_lock_timeout_Str != null && old_lock_timeout_Str.equals(currtLockTimeoutStr)) {
                        //多线程运行时，多个线程签好都到了这里，但只有一个线程的设置值和当前值相同，它才有权利获取锁
                        //设置超时间，释放内存
                        redisTemplate.expire(lockKey, LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
                        //返回加锁时间
                        return lockTimeout;
                    }
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解锁
     *
     * @param lockKey
     * @param lockValue
     * @param threadName
     */
    public void unlock(String lockKey, long lockValue, String threadName) {
        //正常直接删除 如果异常关闭判断加锁会判断过期时间
        //获取redis中设置的时间
        Object result = redisTemplate.opsForValue().get(lockKey);
        Long currtLockTimeoutStr = result == null ? null : Long.valueOf(String.valueOf(result));

        //如果是加锁者，则删除锁， 如果不是，则等待自动过期，重新竞争加锁
        if (currtLockTimeoutStr != null && currtLockTimeoutStr == lockValue) {
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 多服务器集群，使用下面的方法，代替System.currentTimeMillis()，获取redis时间，避免多服务的时间不一致问题！！！
     *
     * @return
     */
    public long currtTimeForRedis() {
        return (Long) redisTemplate.execute((RedisCallback<Long>) redisConnection -> redisConnection.time());
    }
}
