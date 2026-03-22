-- Lua script : KEYS[1] = redisKey, ARGV[1] = current time, ARGV[2] = BUCKET_SIZE, ARGV[3] = refillRate
local key = KEYS[1]
local currentMillis = tonumber(ARGV[1])
local maxBucketSize = tonumber(ARGV[2])
local refillRate = tonumber(ARGV[3])

local token = redis.call('HGET', key, 'token')
local lastUsedAt = redis.call('HGET', key, 'last_used')

if not token or not lastUsedAt then 
    -- Initialize
    redis.call('HSET', key, 'token', maxBucketSize, 'last_used', currentMillis)
    redis.call('EXPIRE', key, 600)
    token = maxBucketSize
else 
    token = tonumber(token)
    lastUsedAt = tonumber(lastUsedAt)
    local minutesPassed = (currentMillis - lastUsedAt) / 60000
    local tokensToAdd = math.floor(minutesPassed * refillRate)

    if tokensToAdd > 0 then
        token = math.min(token + tokensToAdd, maxBucketSize)
        redis.call('HSET', key, 'token', token, 'last_used', currentMillis)
    end
    redis.call('EXPIRE', key, 600)
end

if token > 0 then
    redis.call('HINCRBY', key, 'token', -1)
    return 1
else 
    return 0

end