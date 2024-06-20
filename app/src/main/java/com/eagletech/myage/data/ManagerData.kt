package com.eagletech.myage.data
import android.content.Context
import android.content.SharedPreferences

class ManagerData constructor(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences("MyDataPref", Context.MODE_PRIVATE)
    }

    companion object {
        @Volatile
        private var instance: ManagerData? = null

        fun getInstance(context: Context): ManagerData {
            return instance ?: synchronized(this) {
                instance ?: ManagerData(context).also { instance = it }
            }
        }
    }

    // Lấy ra thông tin mua theo lượt
    fun getData(): Int {
        return sharedPreferences.getInt("data", 0)
    }

    fun setData(data: Int) {
        sharedPreferences.edit().putInt("data", data).apply()
    }

    fun addData(amount: Int) {
        val current = getData()
        setData(current + amount)
    }

    fun removeData() {
        val current = getData()
        if (current > 0) {
            setData(current - 1)
        }
    }


    // Lấy thông tin mua premium
    var isPremium: Boolean?
        get() {
            val userId = sharedPreferences.getString("UserId", "")
            return sharedPreferences.getBoolean("PremiumPlan_\$userId$userId", false)
        }
        set(state) {
            val userId = sharedPreferences.getString("UserId", "")
            sharedPreferences.edit().putBoolean("PremiumPlan_\$userId$userId", state!!).apply()
        }

    // Lưu thông tin người dùng
    fun userId(id: String?) {
        sharedPreferences.edit().putString("UserId", id).apply()
    }

    // Lấy ra thông tin id người dùng
    fun getUserId(): String? {
        return sharedPreferences.getString("UserId", null)
    }

}