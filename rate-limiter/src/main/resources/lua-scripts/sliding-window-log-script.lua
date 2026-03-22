-- Lua script: KEYS[1] = redisKey, ARGV[1] = windowStart, ARGV[2] = currentMillis, ARGV[3] = maxRequests
local key = KEYS[1]
local windowStart = tonumber(ARGV[1])
local currentMillis = tonumber(ARGV[2])
local maxRequests = tonumber(ARGV[3])

-- Remove entries older than windowStart (score < windowStart)
redis.call('ZREMRANGEBYSCORE', key, '-inf', windowStart)
redis.call('EXPIRE', key, 600)

-- Get current size
local currentSize = redis.call('ZCARD', key)
            
-- If size >= maxRequests, deny
if currentSize >= maxRequests then
    return 0  -- false
end

-- Add current timestamp
redis.call('ZADD', key, currentMillis, currentMillis)
return 1  -- true