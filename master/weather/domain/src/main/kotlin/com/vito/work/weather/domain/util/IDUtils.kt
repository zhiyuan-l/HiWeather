package com.vito.work.weather.domain.util

import java.security.MessageDigest


/**
 * Created by lingzhiyuan.
 * Date : 16/4/12.
 * Time : 下午7:04.
 * Description:
 *
 */

fun idGenerator(value: String): String
{
    return encodeByMD5(value)
}

//对字符串进行MD5编码
private fun encodeByMD5(originstr: String): String
{
    try{
        //创建具有指定算法名称的信息摘要
        val md = MessageDigest.getInstance("MD5");
        //使用指定的字节数组对摘要进行最后的更新，然后完成摘要计算
        val results = md.digest(originstr.toByteArray());
        //将得到的字节数组编程字符窜返回
        val resultString = byteArrayToHexString(results);
        return resultString.toUpperCase();
    }catch(ex: Exception){
        ex.printStackTrace();
    }
    return "";
}
//转换字节数组为十六进制字符串
private fun byteArrayToHexString( b: ByteArray): String
{
    var resultsb = StringBuffer();
    for(index in 0..b.size-1)
    {
        resultsb.append(byteToHexString(b[index]));
    }
    return resultsb.toString();
}
//将字节转化成十六进制的字符串
private fun byteToHexString( b: Byte): String
{
    //16进制下数字到字符的映射数组
    val hexDigits =  arrayOf("0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f");

    var n: Int = b.toInt();
    if(n<0)
    {
        n = n + 256;
    }
    var d1 = n / 16;
    var d2 = n /16;
    return hexDigits[d1]+hexDigits[d2];
}
