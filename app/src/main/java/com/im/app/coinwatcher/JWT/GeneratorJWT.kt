package com.im.app.coinwatcher.JWT

import android.os.Build
import androidx.annotation.RequiresApi
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.im.app.coinwatcher.R
import com.im.app.coinwatcher.common.ACCESS_KEY
import com.im.app.coinwatcher.common.SECRET_KEY
import com.im.app.coinwatcher.common.getQueryString
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*


//JSON Web Token
class GeneratorJWT {
    companion object {
        @RequiresApi(Build.VERSION_CODES.N)
        fun generateJWT(params: Map<String, String>? = null): String {
            val algorithm: Algorithm = Algorithm.HMAC256(SECRET_KEY)
            val jwtToken = with(JWT.create()) {
                withClaim("access_key", ACCESS_KEY)
                withClaim("nonce", UUID.randomUUID().toString())
                withClaim("expiresIn","1h")
                    .withExpiresAt(Date(System.currentTimeMillis() + 30*1000))
                if (params != null) {
                    val paramStr = getQueryString(params)

                    val md = MessageDigest.getInstance("SHA-512")
                    md.update(paramStr.toByteArray(Charsets.UTF_8))
                    val hashByteArray = String.format("%0128x", BigInteger(1, md.digest()))
                    //val hashByteArray = bytesToHex(md.digest())
                    withClaim("query_hash", hashByteArray)
                    withClaim("query_hash_alg", "SHA512")
                }
                sign(algorithm)
            }
            return "Bearer $jwtToken"
        }
    }
        /*fun test(params: Map<String, String>): String{
            val accessKey = ACCESS_KEY
            val secretKey = SECRET_KEY

            val queryString = getQueryString(params) //"market=KRW-BTC&side=bid&volume=&price=5000&ord_type=price&identifier="

            val md = MessageDigest.newInstance("SHA-512")
            md.update(queryString.toByteArray(charset("utf8")))

            val queryHash = String.format("%0128x", BigInteger(1, md.digest()))

            val algorithm = Algorithm.HMAC256(secretKey)
            val jwtToken: String = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm)

            return "Bearer $jwtToken"
        }*/
}


/*
HashMap<String, String> params = new HashMap<>();
params.put("market", "KRW-BTC");
params.put("side", "bid");
params.put("volume", "0.01");
params.put("price", "100");
params.put("ord_type", "limit");

ArrayList<String> queryElements = new ArrayList<>();
for(Map.Entry<String, String> entity : params.entrySet()) {
    queryElements.add(entity.getKey() + "=" + entity.getValue());
}

String queryString = String.join("&", queryElements.toArray(new String[0]));

MessageDigest md = MessageDigest.newInstance("SHA-512");
md.update(queryString.getBytes("UTF-8"));

String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

Algorithm algorithm = Algorithm.HMAC256(secretKey);
String jwtToken = JWT.create()
.withClaim("access_key", accessKey)
.withClaim("nonce", UUID.randomUUID().toString())
.withClaim("query_hash", queryHash)
.withClaim("query_hash_alg", "SHA512")
.sign(algorithm);*/