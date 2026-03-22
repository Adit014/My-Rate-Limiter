-- Lua script : KEYS[1] = redisKey, ARGV[1] = current time, ARGV[2] = BUCKET_SIZE, ARGV[3] = leakrate
local key = KEYS[1]
local currentMillis = tonumber(ARGV[1])
local maxBucketSize = tonumber(ARGV[2])
local leakRate = tonumber(ARGV[3])


local token = redis.call('HGET', key, 'token')
local lastLeakedAt = redis.call('HGET', key, 'last_leaked_at')

if not token or not lastLeakedAt then
    redis.call('HSET', key, 'token', 0, 'last_leaked_at', currentMillis)
    redis.call('EXPIRE', key, 600)
    token = 0
else 
    token = tonumber(token)
    lastLeakedAt = tonumber(lastLeakedAt)

    local minutesPassed = (currentMillis - lastLeakedAt) / 60000
    local tokensToLeak = math.floor(minutesPassed * leakRate)

    if (tokensToLeak > 0) then
        token = math.max(0, token - tokensToLeak)
        redis.call('HSET', key, 'token', token, 'last_leaked_at', currentMillis)
    end
    redis.call('EXPIRE', key, 600)
end

if token < maxBucketSize then
    redis.call('HINCRBY', key, 'token', 1)
    return 1
else
    return 0
end