package com.example.bts_task.data.db

object SeedData {
    fun build(): List<OrderEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            OrderEntity(
                number = "F15306",
                pickupAddress = "Toshkent, Amir Temur ko'chasi 1",
                pickupLat = 41.311081,
                pickupLng = 69.240562,
                destinationAddress = "Toshkent, Chilonzor 5",
                destinationLat = 41.275124,
                destinationLng = 69.203569,
                createdAt = now - 1_000
            ),
            OrderEntity(
                number = "F15307",
                pickupAddress = "Samarqand, Registon",
                pickupLat = 39.654651,
                pickupLng = 66.975826,
                destinationAddress = "Samarqand, Bibi Xonim",
                destinationLat = 39.661869,
                destinationLng = 66.978920,
                createdAt = now - 2_000
            ),
            OrderEntity(
                number = "F15308",
                pickupAddress = "Buxoro, Lyabi Hauz",
                pickupLat = 39.774761,
                pickupLng = 64.421194,
                destinationAddress = "Buxoro, Ark qal'asi",
                destinationLat = 39.778060,
                destinationLng = 64.408836,
                createdAt = now - 3_000
            )
        )
    }
}
