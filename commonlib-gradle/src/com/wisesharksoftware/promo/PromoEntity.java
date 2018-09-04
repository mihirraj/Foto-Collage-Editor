package com.wisesharksoftware.promo;

public class PromoEntity {
	public String package_;
	public String img_url;
	public String market_url;
	public PromoEntity(String package_, String img_url, String market_url) {
		super();
		this.package_ = package_;
		this.img_url = img_url;
		this.market_url = market_url;
	}
}
