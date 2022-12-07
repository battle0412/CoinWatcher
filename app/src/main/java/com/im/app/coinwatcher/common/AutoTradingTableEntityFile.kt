package com.im.app.coinwatcher.common


//sqlite
const val DATABASE_NAME = "AUTO_TRADING.db"
const val CURRENT_DB_VERSION = 1
const val TABLE_TRADING_HD = "auto_trading_tbl_hd"
const val TABLE_TRADING_DT = "auto_trading_tbl_dt"
const val CREATE_TRADING_HD_TABLE = """
    CREATE TABLE $TABLE_TRADING_HD (
    uuid TEXT PRIMARY KEY,  
    side TEXT ,             
    ord_type TEXT ,         
    price TEXT ,            
    state TEXT ,            
    market TEXT ,           
    created_at TEXT ,       
    volume TEXT ,           
    remaining_volume TEXT , 
    reserved_fee TEXT ,     
    remaining_fee TEXT ,    
    paid_fee TEXT ,         
    locked TEXT ,           
    executed_volume TEXT ,  
    trades_count INTEGER    
    );
"""
const val CREATE_TRADING_DT_TABLE = """
    CREATE TABLE $TABLE_TRADING_DT (
    uuid TEXT ,          
    trades_uuid TEXT ,   
    side TEXT ,                     
    ord_type TEXT ,                 
    price TEXT ,                    
    state TEXT ,                    
    market TEXT ,                   
    created_at TEXT ,               
    volume TEXT ,                   
    remaining_volume TEXT ,         
    reserved_fee TEXT ,             
    remaining_fee TEXT ,            
    paid_fee TEXT ,                 
    locked TEXT ,                   
    executed_volume TEXT ,          
    trades_count INTEGER ,
    trades_market TEXT ,
    trades_price TEXT ,             
    trades_volume TEXT ,            
    trades_funds TEXT ,             
    trades_side TEXT ,              
    trades_created_at TEXT ,
    trades_trend TEXT ,
    PRIMARY KEY(uuid, trades_uuid)
    );
"""