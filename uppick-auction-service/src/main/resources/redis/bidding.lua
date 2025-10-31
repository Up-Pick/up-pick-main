local current_bid_price_key = KEYS[1]
local last_bidder_id_key = KEYS[2]
local price = ARGV[1]
local bidder_id = ARGV[2]

redis.call('SET', current_bid_price_key, price)
redis.call('SET', last_bidder_id_key, bidder_id)