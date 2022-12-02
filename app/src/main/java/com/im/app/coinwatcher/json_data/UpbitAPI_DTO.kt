package com.im.app.coinwatcher.json_data


/*
시세 캔들 조회 -> 분(Minute)캔들
market	                마켓명                    String
candle_date_time_utc	캔들 기준 시각(UTC 기준)    포맷: yyyy-MM-dd'T'HH:mm:ss	String
candle_date_time_kst	캔들 기준 시각(KST 기준)    포맷: yyyy-MM-dd'T'HH:mm:ss	String
opening_price	        시가                      Double
high_price	            고가                      Double
low_price	            저가                      Double
trade_price	            종가                      Double
timestamp	            해당 캔들에서 마지막 틱이 저장된 시각	Long
candle_acc_trade_price	누적 거래 금액              Double
candle_acc_trade_volume	누적 거래량                 Double
unit	                분 단위(유닛)               Integer
*/
data class MinuteCandles(
    var market: String,
    var candle_date_time_utc: String,
    var candle_date_time_kst: String,
    var opening_price: Double,
    var high_price: Double,
    var low_price: Double,
    var trade_price: Double,
    var timestamp: Long,
    var candle_acc_trade_price: Double,
    var candle_acc_trade_volume: Double,
    var unit: Integer
)
/*
시세 캔들 조회 -> 일(Day)주(Week)월(Month)캔들
market	                마켓명                    String
candle_date_time_utc	캔들 기준 시각(UTC 기준)    포맷: yyyy-MM-dd'T'HH:mm:ss	String
candle_date_time_kst	캔들 기준 시각(KST 기준)    포맷: yyyy-MM-dd'T'HH:mm:ss	String
opening_price	        시가                      Double
high_price	            고가                      Double
low_price	            저가                      Double
trade_price	            종가                      Double
timestamp	            마지막 틱이 저장된 시각      Long
candle_acc_trade_price	누적 거래 금액	         Double
candle_acc_trade_volume	누적 거래량	             Double
prev_closing_price	    전일 종가(UTC 0시 기준)	 Double(일 캔들)
change_price	        전일 종가 대비 변화 금액	 Double(일 캔들)
change_rate	            전일 종가 대비 변화량	     Double(일 캔들)
converted_trade_price	종가 환산 화폐 단위로 환산된 가격(요청에 convertingPriceUnit 파라미터 없을 시 해당 필드 포함되지 않음.)	Double(일 캔들)
first_day_of_period	    캔들 기간의 가장 첫 날       String(주,월 캔들)
*/
data class Candles(
    var market: String,
    var candle_date_time_utc: String,
    var candle_date_time_kst: String,
    var opening_price: Double,
    var high_price: Double,
    var low_price: Double,
    var trade_price: Double,
    var timestamp: Long,
    var candle_acc_trade_price: Double,
    var candle_acc_trade_volume: Double,
    var prev_closing_price: Double,
    var change_price: Double,
    var change_rate: Double,
    var converted_trade_price: Double,
    var first_day_of_period: String
)

/*
주문 -> 주문하기
uuid	            주문의 고유 아이디	        String
side	            주문 종류	                String
ord_type	        주문 방식	                String
price	            주문 당시 화폐 가격	    NumberString
state	            주문 상태	                String
market	            마켓의 유일키	            String
created_at	        주문 생성 시간	        String
volume	            사용자가 입력한 주문 양	    NumberString
remaining_volume	체결 후 남은 주문 양	    NumberString
reserved_fee	    수수료로 예약된 비용	    NumberString
remaining_fee	    남은 수수료	            NumberString
paid_fee	        사용된 수수료	            NumberString
locked	            거래에 사용중인 비용	    NumberString
executed_volume	    체결된 양	                NumberString
trades_count	    해당 주문에 걸린 체결 수	Integer
 */
data class Orders(
    var uuid: String,
    var side: String,
    var ord_type: String,
    var price: String,
    var state: String,
    var market: String,
    var created_at: String,
    var volume: String,
    var remaining_volume: String,
    var reserved_fee: String,
    var paid_fee: String,
    var executed_volume: String,
    var trades_count: Integer
)
/*
자산 -> 전체 계좌 조회
currency	            화폐를 의미하는 영문 대문자 코드	String
balance	                주문가능 금액/수량	            NumberString
locked	                주문 중 묶여있는 금액/수량	    NumberString
avg_buy_price	        매수평균가	                NumberString
avg_buy_price_modified	매수평균가 수정 여부	        Boolean
unit_currency	        평단가 기준 화폐	            String
*/
data class Accounts(
    var currency: String,
    var balance: String,
    var locked: String,
    var avg_buy_price: String,
    var avg_buy_price_modified: Boolean,
    var unit_currency: String
)

/*
market	        업비트에서 제공중인 시장 정보	String
korean_name	    거래 대상 암호화폐 한글명	    String
english_name	거래 대상 암호화폐 영문명	    String
market_warning	유의 종목 여부
NONE (해당 사항 없음), CAUTION(투자유의)	    String
 */
data class MarketAll(
    var market: String,
    var korean_name: String,
    var english_name: String,
    var market_warning: String
)

/*
market	                종목 구분 코드	String
trade_date	            최근 거래 일자(UTC) 포맷: yyyyMMdd	String
trade_time	            최근 거래 시각(UTC) 포맷: HHmmss	String
trade_date_kst	        최근 거래 일자(KST) 포맷: yyyyMMdd	String
trade_time_kst	        최근 거래 시각(KST) 포맷: HHmmss	String
trade_timestamp	        최근 거래 일시(UTC) 포맷: Unix Timestamp	Long
opening_price	        시가	Double
high_price	            고가	Double
low_price	            저가	Double
trade_price	            종가(현재가)	Double
prev_closing_price	    전일 종가(UTC 0시 기준)	Double
change	                EVEN : 보합 RISE : 상승 FALL : 하락	String
change_price	        변화액의 절대값	Double
change_rate	            변화율의 절대값	Double
signed_change_price	    부호가 있는 변화액	Double
signed_change_rate	    부호가 있는 변화율	Double
trade_volume	        가장 최근 거래량	Double
acc_trade_price	        누적 거래대금(UTC 0시 기준)	Double
acc_trade_price_24h	    24시간 누적 거래대금	Double
acc_trade_volume	    누적 거래량(UTC 0시 기준)	Double
acc_trade_volume_24h	24시간 누적 거래량	Double
highest_52_week_price	52주 신고가	Double
highest_52_week_date	52주 신고가 달성일 포맷: yyyy-MM-dd	String
lowest_52_week_price	52주 신저가	Double
lowest_52_week_date	    52주 신저가 달성일 포맷: yyyy-MM-dd	String
timestamp	            타임스탬프	Long
*위 응답의 change, change_price, change_rate, signed_change_price, signed_change_rate 필드들은 전일종가 대비 값입니다.
 */
data class MarketTicker(
    var market: String,
    var trade_date: String,
    var trade_time: String,
    var trade_date_kst: String,
    var trade_time_kst: String,
    var trade_timestamp: Long,
    var opening_price: Double,
    var high_price: Double,
    var low_price: Double,
    var trade_price: Double,
    var prev_closing_price: Double,
    var change: String,
    var change_price: Double,
    var change_rate: Double,
    var signed_change_price: Double,
    var signed_change_rate: Double,
    var trade_volume: Double,
    var acc_trade_price: Double,
    var acc_trade_price_24h: Double,
    var acc_trade_volume: Double,
    var acc_trade_volume_24h: Double,
    var highest_52_week_price: Double,
    var highest_52_week_date: String,
    var lowest_52_week_price: Double,
    var lowest_52_week_date: String,
    var timestamp: Long
)

data class ReqOrder(
    /*@Query("market") market: String,
        @Query("side") side: String,
        @Query("volume") volume: String,
        @Query("price") price: String,
        @Query("ord_type") ord_type: String,
        @Query("identifier") identifier: String*/
    var market: String,
    var side: String,
    var volume: String,
    var price: String,
    var ord_type: String,
    var identifier: String
)
