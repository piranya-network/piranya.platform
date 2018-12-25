package network.piranya.platform.node.core.info;

import java.math.BigDecimal;

public class CoinApiSymbol {
	
	private String symbol_id;
	private String exchange_id;
	private String symbol_type;
	private String asset_id_base;
	private String asset_id_quote;
	private String data_start;
	private String data_end;
	private String data_quote_start;
	private String data_quote_end;
	private String data_orderbook_start;
	private String data_orderbook_end;
	private String data_trade_start;
	private String data_trade_end;
	private String future_delivery_time;
	private boolean option_type_is_call;
	private BigDecimal option_strike_price;
	private BigDecimal option_contract_unit;
	private String option_exercise_style;
	private String option_expiration_time;
	
	public String getSymbol_id() {
		return symbol_id;
	}
	public void setSymbol_id(String symbol_id) {
		this.symbol_id = symbol_id;
	}
	public String getExchange_id() {
		return exchange_id;
	}
	public void setExchange_id(String exchange_id) {
		this.exchange_id = exchange_id;
	}
	public String getSymbol_type() {
		return symbol_type;
	}
	public void setSymbol_type(String symbol_type) {
		this.symbol_type = symbol_type;
	}
	public String getAsset_id_base() {
		return asset_id_base;
	}
	public void setAsset_id_base(String asset_id_base) {
		this.asset_id_base = asset_id_base;
	}
	public String getAsset_id_quote() {
		return asset_id_quote;
	}
	public void setAsset_id_quote(String asset_id_quote) {
		this.asset_id_quote = asset_id_quote;
	}
	public String getData_start() {
		return data_start;
	}
	public void setData_start(String data_start) {
		this.data_start = data_start;
	}
	public String getData_end() {
		return data_end;
	}
	public void setData_end(String data_end) {
		this.data_end = data_end;
	}
	public String getData_quote_start() {
		return data_quote_start;
	}
	public void setData_quote_start(String data_quote_start) {
		this.data_quote_start = data_quote_start;
	}
	public String getData_quote_end() {
		return data_quote_end;
	}
	public void setData_quote_end(String data_quote_end) {
		this.data_quote_end = data_quote_end;
	}
	public String getData_orderbook_start() {
		return data_orderbook_start;
	}
	public void setData_orderbook_start(String data_orderbook_start) {
		this.data_orderbook_start = data_orderbook_start;
	}
	public String getData_orderbook_end() {
		return data_orderbook_end;
	}
	public void setData_orderbook_end(String data_orderbook_end) {
		this.data_orderbook_end = data_orderbook_end;
	}
	public String getData_trade_start() {
		return data_trade_start;
	}
	public void setData_trade_start(String data_trade_start) {
		this.data_trade_start = data_trade_start;
	}
	public String getData_trade_end() {
		return data_trade_end;
	}
	public void setData_trade_end(String data_trade_end) {
		this.data_trade_end = data_trade_end;
	}
	public String getFuture_delivery_time() {
		return future_delivery_time;
	}
	public void setFuture_delivery_time(String future_delivery_time) {
		this.future_delivery_time = future_delivery_time;
	}
	public boolean isOption_type_is_call() {
		return option_type_is_call;
	}
	public void setOption_type_is_call(boolean option_type_is_call) {
		this.option_type_is_call = option_type_is_call;
	}
	public BigDecimal getOption_strike_price() {
		return option_strike_price;
	}
	public void setOption_strike_price(BigDecimal option_strike_price) {
		this.option_strike_price = option_strike_price;
	}
	public BigDecimal getOption_contract_unit() {
		return option_contract_unit;
	}
	public void setOption_contract_unit(BigDecimal option_contract_unit) {
		this.option_contract_unit = option_contract_unit;
	}
	public String getOption_exercise_style() {
		return option_exercise_style;
	}
	public void setOption_exercise_style(String option_exercise_style) {
		this.option_exercise_style = option_exercise_style;
	}
	public String getOption_expiration_time() {
		return option_expiration_time;
	}
	public void setOption_expiration_time(String option_expiration_time) {
		this.option_expiration_time = option_expiration_time;
	}
	
}
