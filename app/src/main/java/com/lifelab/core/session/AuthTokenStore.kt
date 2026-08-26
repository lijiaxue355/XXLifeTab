package com.lifelab.core.session

import com.tencent.mmkv.MMKV

class AuthTokenStore {
    private val mmkv: MMKV = requireNotNull(MMKV.defaultMMKV()){
        "MMKV没初始化"
    }
    fun saveAccessToken(token: String){
        mmkv.encode(KEY_ACCESS_TOKEN,token)
    }
    fun getAccessToken() : String?{
        return mmkv.decodeString(KEY_ACCESS_TOKEN)
    }

    fun clearAccessToken() {
        mmkv.removeValueForKey(KEY_ACCESS_TOKEN)
    }

    fun hasAccessToken(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    companion object{
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

}