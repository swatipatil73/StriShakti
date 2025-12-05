package com.collage.empowermentstrishakti.data.model.Groups



import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class GroupDetailsResponse(
    val hasNextPage: Boolean,
    val totalPages: Int,
    val pageSize: Int,
    val nextPageNo: Int,
    val groupAbout: GroupAbout?,
    val currentPage: Int,
    val currentGroupMembers: List<GroupMember>,
    val postDetails: List<PostDetail>,
    val totalElements: Int
): Parcelable



