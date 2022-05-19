package com.ssafy.smartstore.databases

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssafy.smartstore.dto.OrderDetail

@Dao
interface OrderDetailDao {
    // product: img, name, price
    // order_detail: quantity
    @Query("SELECT p.img, p.name, p.price, d.quantity " +
                " FROM t_order_detail d, t_product p, t_order o "+
                " WHERE o.id = (:order_id) " +
            "AND d.order_id = o.id " +
            "AND d.product_id = p.id")
    suspend fun getDetail(order_id: Int): MutableList<Shopping>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: OrderDetail)

    @Query("SELECT id FROM t_order_detail ORDER BY id DESC LIMIT 1;")
    suspend fun orderSize(): Int

}