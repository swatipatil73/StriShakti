package com.collage.new_strishakti.data.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class PageListResponse(
    @SerializedName("hasNextPage")
    val hasNextPage: Boolean,

    @SerializedName("pagesDetail")
    val pagesDetail: List<PageDetail> = emptyList(),

    @SerializedName("totalPages")
    val totalPages: Int = 0,

    @SerializedName("pageSize")
    val pageSize: Int = 0,

    @SerializedName("nextPageNo")
    val nextPageNo: Int = 0,

    @SerializedName("currentPage")
    val currentPage: Int = 0,

    @SerializedName("totalElements")
    val totalElements: Int = 0
)

@Parcelize
data class PageDetail(
    @SerializedName("adminUserFirstName")
    val adminUserFirstName: String? = null,

    @SerializedName("pagesId")
    val pagesId: Int = 0,

    @SerializedName("puuid")
    val puuid: String? = null,

    @SerializedName("pageCoverProfileImagePath")
    val pageCoverProfileImagePath: String? = null,

    @SerializedName("isPageFollowed")
    val isPageFollowed: Boolean = false,

    @SerializedName("pageName")
    val pageName: String? = null,

    @SerializedName("pageCreatedAt")
    val pageCreatedAt: String? = null, // keep ISO string; format/parse in UI

    @SerializedName("linkUrlName")
    val linkUrlName: String? = null,

    @SerializedName("adminUserLastName")
    val adminUserLastName: String? = null,

    @SerializedName("adminUserProfileImagePath")
    val adminUserProfileImagePath: String? = null,

    @SerializedName("adminId")
    val adminId: Int = 0,

    @SerializedName("linkUrl")
    val linkUrl: String? = null,

    @SerializedName("pageDescription")
    val pageDescription: String? = null
) : Parcelable {
    // Convenience helpers
    fun getAdminFullName(): String {
        val first = adminUserFirstName ?: ""
        val last = adminUserLastName ?: ""
        return listOf(first, last).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Unknown" }
    }
}