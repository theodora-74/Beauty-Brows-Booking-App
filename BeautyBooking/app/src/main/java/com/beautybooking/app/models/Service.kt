package com.beautybooking.app.models

import android.content.Context
import com.beautybooking.app.R

data class Service(
    val nameResId: Int,
    val iconResId: Int,
    val key: String,
    val hasSubOptions: Boolean = false
) {
    fun getLocalizedName(context: Context): String = context.getString(nameResId)

    companion object {
        fun getAllServices(): List<Service> = listOf(
            Service(R.string.service_haircut, R.drawable.ic_service_haircut, "haircut"),
            Service(R.string.service_hair_coloring, R.drawable.ic_service_coloring, "hair_coloring"),
            Service(R.string.service_lash_lift, R.drawable.ic_service_lash, "lash_lift"),
            Service(R.string.service_brow_lift, R.drawable.ic_service_brow, "brow_lift"),
            Service(R.string.service_lash_extensions, R.drawable.ic_service_extensions, "lash_extensions"),
            Service(R.string.service_microblading, R.drawable.ic_service_microblading, "microblading"),
            Service(R.string.service_lip_tattoo, R.drawable.ic_service_lip, "lip_tattoo"),
            Service(R.string.service_eyeliner_tattoo, R.drawable.ic_service_eyeliner, "eyeliner_tattoo"),
            Service(R.string.service_brow_clean, R.drawable.ic_service_browclean, "brow_clean"),
            Service(R.string.service_nails, R.drawable.ic_service_nails, "nails", hasSubOptions = true)
        )
    }
}
