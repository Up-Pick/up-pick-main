CREATE INDEX idx_lastbidderid_status_endat ON auction(last_bidder_id ASC, status ASC, end_at DESC);
CREATE INDEX idx_registerid_status_endat ON auction(register_id ASC, status ASC, end_at DESC);