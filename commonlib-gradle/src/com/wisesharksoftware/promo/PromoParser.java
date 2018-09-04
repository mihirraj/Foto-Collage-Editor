package com.wisesharksoftware.promo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PromoParser {
	private static final String PROMO = "promo";
	private static final String PACKAGE = "package";
	private static final String IMG_URL = "img_url";
	private static final String MARKET_URL = "market_url";

	public PromoEntity parse(String jsonData) throws JSONException {
		JSONObject promoJSON = new JSONObject(jsonData);
		String pack_str = promoJSON.getString(PACKAGE);
		String img_url = promoJSON.getString(IMG_URL);
		String market_url = promoJSON.getString(MARKET_URL);
		PromoEntity entity = new PromoEntity(pack_str, img_url, market_url);
		return entity;
	}
}
