CREATE INDEX idx_productid_sellat ON sell_detail (product_id ASC, sell_at DESC);
CREATE INDEX idx_productid_purchaseat ON purchase_detail (product_id ASC, purchase_at DESC);